import re
import json
import heapq
import logging
import configparser
import math
import os
from typing import List, Tuple, Dict, Any, Optional, Union, Set
import mysql.connector
from mysql.connector import Error
import networkx as nx
from flask import Flask, request, jsonify

# --- Helper Types (for type hinting) ---
Coord = Tuple[float, float]
# Path will now be a List of node IDs (integers)
Path = List[int]
NodeData = Dict[str, Any]

# --- Configuration Setup ---
CONFIG_FILE = "config.ini"
config = configparser.ConfigParser()
config.read(CONFIG_FILE)

def get_config_value(section: str, key: str, fallback: Any, type_converter=None):
    """
    Get configuration value with priority: Environment Variable > Config File > Fallback
    Environment variable format: SECTION_KEY (e.g., DATABASE_HOST, FLASK_PORT)
    """
    # Try environment variable first (convert to uppercase and use underscore)
    env_key = f"{section.upper()}_{key.upper()}"
    env_value = os.getenv(env_key)
    
    if env_value is not None:
        # Convert environment variable to appropriate type
        try:
            if type_converter == 'float':
                return float(env_value)
            elif type_converter == 'int':
                return int(env_value)
            elif type_converter == 'boolean':
                return env_value.lower() in ('true', '1', 'yes', 'on')
            else:
                return env_value
        except (ValueError, TypeError) as e:
            logger.warning(f"Invalid environment variable {env_key}={env_value}, using config file fallback: {e}")
    
    # Fall back to config file
    if config.has_option(section, key):
        try:
            if type_converter == 'float':
                return config.getfloat(section, key)
            elif type_converter == 'int':
                return config.getint(section, key)
            elif type_converter == 'boolean':
                return config.getboolean(section, key)
            else:
                return config.get(section, key)
        except (ValueError, TypeError) as e:
            logger.warning(f"Invalid config file value [{section}] {key}, using fallback: {e}")
    
    # Use fallback value
    return fallback

# --- Logging Setup ---
log_level_str = get_config_value('Logging', 'Level', 'INFO').upper()
log_level = getattr(logging, log_level_str, logging.INFO)
logging.basicConfig(level=log_level,
                    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

app = Flask(__name__)

# --- Global Variables ---
# Now a single graph for all floors
COMPOSITE_GRAPH: Optional[nx.Graph] = None
# Maps venue names to their node_id, coordinates, and floor_id
VENUE_LOOKUP: Dict[str, Dict[str, Any]] = {}
# Stores all node attributes keyed by node_id
ALL_NODE_ATTRIBUTES: Dict[int, Dict[str, Any]] = {}
# Maps node IDs to coordinates for quick lookup (if needed by old code, but prefer attributes)
NODE_ID_TO_COORD: Dict[int, Coord] = {}
# Maps coordinates to node IDs (can be ambiguous for multi-floor if (x,y) duplicates)
COORD_TO_NODE_ID: Dict[Coord, int] = {}
# Store stair node details
STAIR_NODES: Dict[int, Dict[str, Any]] = {} # Maps node_id to stair node data

# --- Constants ---
# Define the straightThreshold (10 degrees in radians)
STRAIGHT_THRESHOLD = 10 * (math.pi / 180)

# IMPORTANT: ASSUMPTION FOR DISTANCE SCALING
# This value determines how many coordinate units equate to 1 meter.
# For example, if 100 units on your map represent 1 meter, set this to 0.01 (1/100).
# If 1 unit on your map represents 1 meter, set this to 1.0.
# You will need to replace this with your measured ratio next week.
# For now, I'm assuming 1 coordinate unit equals 0.1 meters.
DISTANCE_SCALE_FACTOR = 0.01 # Placeholder: 1 unit on map = 0.1 meters

# --- Database Connection ---
def get_db_connection():
    """Create and return a database connection."""
    try:
        connection = mysql.connector.connect(
            host=get_config_value('Database', 'Host', 'localhost'),
            database=get_config_value('Database', 'Name', 'event-hub-navigation'),
            user=get_config_value('Database', 'User', 'root'),
            password=get_config_value('Database', 'Password', ''),
            port=get_config_value('Database', 'Port', 3306, 'int')
        )
        return connection
    except Error as e:
        logger.error(f"Error connecting to MySQL database: {e}")
        return None

def heuristic(node_id_a: int, node_id_b: int) -> float:
    """
    Computes heuristic distance between nodes for multi-level navigation.
    Uses 2D Euclidean distance for same-floor nodes, and estimates realistic
    inter-floor travel cost for different-floor nodes.
    """
    if node_id_a not in ALL_NODE_ATTRIBUTES or node_id_b not in ALL_NODE_ATTRIBUTES:
        logger.error(f"Heuristic: Node IDs not found in ALL_NODE_ATTRIBUTES: {node_id_a}, {node_id_b}")
        return float('inf')

    floor_a = ALL_NODE_ATTRIBUTES[node_id_a]['floor_plan_id']
    floor_b = ALL_NODE_ATTRIBUTES[node_id_b]['floor_plan_id']
    coord_a = (ALL_NODE_ATTRIBUTES[node_id_a]['x_coord'], ALL_NODE_ATTRIBUTES[node_id_a]['y_coord'])
    coord_b = (ALL_NODE_ATTRIBUTES[node_id_b]['x_coord'], ALL_NODE_ATTRIBUTES[node_id_b]['y_coord'])

    # Standard 2D Euclidean distance
    horizontal_dist = ((coord_a[0] - coord_b[0])**2 + (coord_a[1] - coord_b[1])**2) ** 0.5

    # If on the same floor, return 2D distance
    if floor_a == floor_b:
        return horizontal_dist

    # For different floors, estimate realistic travel cost
    floor_diff = abs(floor_a - floor_b)
    
    # Get stair traversal weight from config (same as used in inter_floor_connection)
    stair_weight = get_config_value('Graph', 'StairTraversalWeight', 5.0, 'float')
    
    # Estimate inter-floor heuristic:
    # 1. Vertical cost: number of floors * stair traversal weight
    vertical_cost = floor_diff * stair_weight
    
    # 2. Horizontal cost: Use the 2D distance as base, but add some penalty
    #    to account for the fact that you might need to take a longer horizontal
    #    path on intermediate floors to reach the right stair
    horizontal_penalty_factor = get_config_value('Graph', 'InterFloorHorizontalPenalty', 1.2, 'float')
    adjusted_horizontal_cost = horizontal_dist * horizontal_penalty_factor
    
    # 3. Total estimated cost
    total_heuristic = vertical_cost + adjusted_horizontal_cost
    
    logger.debug(f"Inter-floor heuristic: {node_id_a}(F{floor_a}) -> {node_id_b}(F{floor_b}) = "
                f"vertical({vertical_cost:.2f}) + horizontal({adjusted_horizontal_cost:.2f}) = {total_heuristic:.2f}")
    
    return total_heuristic

def a_star_search(graph: nx.Graph, start_node_id: int, goal_node_id: int) -> Optional[Path]:
    """A* search algorithm. Returns the path (list of node IDs) or None if not found."""

    logger.info(f"A* search initiated: Start Node ID={start_node_id}, Goal Node ID={goal_node_id}")

    if start_node_id not in graph or goal_node_id not in graph:
        missing_nodes = []
        if start_node_id not in graph:
            missing_nodes.append(f"Start node ID {start_node_id}")
        if goal_node_id not in graph:
            missing_nodes.append(f"Goal node ID {goal_node_id}")

        logger.error(f"A* search failed: {', '.join(missing_nodes)} not found in graph.")
        logger.info(f"Graph contains {graph.number_of_nodes()} nodes total.")
        # Logging nearby nodes for debugging if needed (original code had this, adapting to new IDs)
        # if start_node_id not in graph and start_node_id in ALL_NODE_ATTRIBUTES:
        #     nearby_start = find_closest_nodes(graph, (ALL_NODE_ATTRIBUTES[start_node_id]['x_coord'], ALL_NODE_ATTRIBUTES[start_node_id]['y_coord']), 5)
        #     logger.info(f"Closest nodes to start {start_node_id}: {nearby_start}")
        # if goal_node_id not in graph and goal_node_id in ALL_NODE_ATTRIBUTES:
        #     nearby_goal = find_closest_nodes(graph, (ALL_NODE_ATTRIBUTES[goal_node_id]['x_coord'], ALL_NODE_ATTRIBUTES[goal_node_id]['y_coord']), 5)
        #     logger.info(f"Closest nodes to goal {goal_node_id}: {nearby_goal}")
        return None

    if start_node_id == goal_node_id:
        logger.info("A* search: Start and goal are the same node ID.")
        return [start_node_id]

    priority_queue: List[Tuple[float, int]] = [] # (priority, node_id)
    heapq.heappush(priority_queue, (0, start_node_id))
    came_from: Dict[int, Optional[int]] = {start_node_id: None}
    cost_so_far: Dict[int, float] = {start_node_id: 0}

    nodes_explored = 0
    max_queue_size = 1
    edges_examined = 0
    skipped_edges = 0

    logger.debug(f"A* search initialized with heuristic distance: {heuristic(start_node_id, goal_node_id):.2f}")

    while priority_queue:
        current_priority, current_node_id = heapq.heappop(priority_queue)
        nodes_explored += 1

        if nodes_explored % 100 == 0:
            logger.debug(f"A* progress: Explored {nodes_explored} nodes, queue size: {len(priority_queue)}")

        max_queue_size = max(max_queue_size, len(priority_queue))

        if current_node_id == goal_node_id:
            logger.info(f"A* search successful: Goal reached after exploring {nodes_explored} nodes.")
            logger.info(f"Search statistics: Max queue size: {max_queue_size}, Edges examined: {edges_examined}, Skipped edges: {skipped_edges}.")
            break

        for neighbor_node_id in graph.neighbors(current_node_id):
            edges_examined += 1
            edge_data = graph.get_edge_data(current_node_id, neighbor_node_id)

            if not edge_data or 'weight' not in edge_data:
                skipped_edges += 1
                logger.warning(f"Edge from {current_node_id} to {neighbor_node_id} missing weight. Skipping. Edge data: {edge_data}")
                continue

            new_cost = cost_so_far[current_node_id] + edge_data['weight']

            if neighbor_node_id not in cost_so_far or new_cost < cost_so_far[neighbor_node_id]:
                cost_so_far[neighbor_node_id] = new_cost
                heuristic_cost = heuristic(neighbor_node_id, goal_node_id)
                priority = new_cost + heuristic_cost
                heapq.heappush(priority_queue, (priority, neighbor_node_id))
                came_from[neighbor_node_id] = current_node_id

                logger.debug(f"Added neighbor {neighbor_node_id}: cost={new_cost:.2f}, heuristic={heuristic_cost:.2f}, priority={priority:.2f}")
    else:
        logger.error(f"A* search failed: Goal {goal_node_id} not reachable from {start_node_id}.")
        return None

    if goal_node_id not in came_from:
        logger.error(f"A* search: Unexpected error - Goal {goal_node_id} not in came_from despite successful search.")
        return None

    path_list: Path = []
    curr_node_id: Optional[int] = goal_node_id
    while curr_node_id is not None:
        path_list.append(curr_node_id)
        curr_node_id = came_from.get(curr_node_id)

    final_path = path_list[::-1]
    logger.info(f"A* search completed: Path found with {len(final_path)} nodes, total cost: {cost_so_far[goal_node_id]:.2f}")
    logger.debug(f"Complete path (node IDs): {final_path}")

    return final_path

def find_closest_nodes(graph: nx.Graph, target_coord: Coord, limit: int = 5) -> List[Tuple[int, float]]:
    """Find the closest nodes (by ID) in the graph to a target coordinate."""
    if not graph.nodes():
        return []

    distances = []
    for node_id in graph.nodes():
        # Ensure node_id exists in ALL_NODE_ATTRIBUTES before accessing
        if node_id in ALL_NODE_ATTRIBUTES:
            node_coords = (ALL_NODE_ATTRIBUTES[node_id]['x_coord'], ALL_NODE_ATTRIBUTES[node_id]['y_coord'])
            distance = ((node_coords[0] - target_coord[0])**2 + (node_coords[1] - target_coord[1])**2) ** 0.5
            distances.append((node_id, distance))
        else:
            logger.warning(f"find_closest_nodes: Node ID {node_id} not found in ALL_NODE_ATTRIBUTES.")


    distances.sort(key=lambda x: x[1])
    return distances[:limit]


# --- Path Simplification Function ---
def get_angle_between_points(p1: Coord, p2: Coord, p3: Coord) -> float:
    """
    Calculates the angle (in radians) between the line segments P1P2 and P2P3.
    Returns the absolute angle between 0 and pi.
    """
    vec1 = (p2[0] - p1[0], p2[1] - p1[1])
    vec2 = (p3[0] - p2[0], p3[1] - p2[1])

    dot_product = vec1[0] * vec2[0] + vec1[1] * vec2[1]
    magnitude_vec1 = math.sqrt(vec1[0]**2 + vec1[1]**2)
    magnitude_vec2 = math.sqrt(vec2[0]**2 + vec2[1]**2)

    if magnitude_vec1 == 0 or magnitude_vec2 == 0:
        return 0.0 # Or handle as an error/special case

    # Clamp the value to avoid domain errors in acos due to floating point inaccuracies
    cosine_angle = max(-1.0, min(1.0, dot_product / (magnitude_vec1 * magnitude_vec2)))

    return math.acos(cosine_angle)

def simplify_path(path_node_ids: Path, straight_threshold: float) -> List[Dict[str, Any]]:
    """
    Simplifies the path (list of node IDs) by removing intermediate nodes in straight lines
    and identifying turns based on an angle threshold.
    Returns a list of path segments, each with start, end, and distance, including floor info.
    """
    if len(path_node_ids) <= 1:
        if not path_node_ids:
            return []
        
        node_id = path_node_ids[0]
        if node_id not in ALL_NODE_ATTRIBUTES:
            logger.error(f"Simplify path: Single node ID {node_id} not found in ALL_NODE_ATTRIBUTES.")
            return []

        return [{
            "start_node_id": node_id,
            "end_node_id": node_id,
            "start_coord": (ALL_NODE_ATTRIBUTES[node_id]['x_coord'], ALL_NODE_ATTRIBUTES[node_id]['y_coord']),
            "end_coord": (ALL_NODE_ATTRIBUTES[node_id]['x_coord'], ALL_NODE_ATTRIBUTES[node_id]['y_coord']),
            "start_floor_id": ALL_NODE_ATTRIBUTES[node_id]['floor_plan_id'],
            "end_floor_id": ALL_NODE_ATTRIBUTES[node_id]['floor_plan_id'],
            "distance_meters": 0.0,
            "type": "destination" # For a single point, it's the destination
        }]

    simplified_segments: List[Dict[str, Any]] = []
    
    current_segment_start_id = path_node_ids[0]
    
    # Iterate through the path to identify segments
    for i in range(len(path_node_ids)):
        current_node_id = path_node_ids[i]
        
        if current_node_id not in ALL_NODE_ATTRIBUTES:
            logger.error(f"Simplify path: Node ID {current_node_id} not found in ALL_NODE_ATTRIBUTES. Skipping segment.")
            continue

        p1_coord = (ALL_NODE_ATTRIBUTES[current_node_id]['x_coord'], ALL_NODE_ATTRIBUTES[current_node_id]['y_coord'])
        
        # Determine if this is the last node, a turn, a stair/elevator, or a floor change
        is_last_node = (i == len(path_node_ids) - 1)
        is_turn = False
        is_stair_or_elevator_node = (ALL_NODE_ATTRIBUTES[current_node_id]['type'] == 'STAIR' or
                                     ALL_NODE_ATTRIBUTES[current_node_id]['type'] == 'ELEVATOR')

        if not is_last_node:
            next_node_id = path_node_ids[i+1]
            if next_node_id not in ALL_NODE_ATTRIBUTES:
                logger.error(f"Simplify path: Next node ID {next_node_id} not found in ALL_NODE_ATTRIBUTES. Skipping segment.")
                continue

            p2_coord = (ALL_NODE_ATTRIBUTES[next_node_id]['x_coord'], ALL_NODE_ATTRIBUTES[next_node_id]['y_coord'])
            
            # Check for turn if there's a third point
            if i < len(path_node_ids) - 2:
                next_next_node_id = path_node_ids[i+2]
                if next_next_node_id not in ALL_NODE_ATTRIBUTES:
                    logger.warning(f"Simplify path: Next-next node ID {next_next_node_id} not found in ALL_NODE_ATTRIBUTES.")
                    # Can't determine angle, assume straight for now
                else:
                    p3_coord = (ALL_NODE_ATTRIBUTES[next_next_node_id]['x_coord'], ALL_NODE_ATTRIBUTES[next_next_node_id]['y_coord'])
                    angle = get_angle_between_points(p1_coord, p2_coord, p3_coord)
                    if not (angle < straight_threshold or abs(angle - math.pi) < straight_threshold):
                        is_turn = True
            
            # Check for inter-floor change
            is_floor_change_at_next_node = (ALL_NODE_ATTRIBUTES[current_node_id]['floor_plan_id'] != ALL_NODE_ATTRIBUTES[next_node_id]['floor_plan_id'])

            # A segment ends if:
            # 1. It's the end of the entire path (handled by is_last_node logic below).
            # 2. A turn is detected (is_turn is True).
            # 3. The current node is a stair/elevator *and* the next node is on a different floor
            #    (indicating an actual inter-floor transition).
            # 4. The *next* node is a stair/elevator (meaning the segment leads *to* a transition point).
            # 5. It's the second to last segment, so the next node is the final destination.
            
            # Condition for segment end (and starting a new segment)
            should_end_segment = (
                is_turn or
                is_floor_change_at_next_node or
                is_stair_or_elevator_node or # If current node is a stair/elevator
                ALL_NODE_ATTRIBUTES[next_node_id]['type'] in ('STAIR', 'ELEVATOR') # If next node is a stair/elevator
            )
            
            # Always end segment before the final destination or if it's the second-to-last node.
            # This ensures the last segment is correctly captured up to the destination.
            if i == len(path_node_ids) - 2:
                should_end_segment = True
            
        else: # This is the very last node in the path, so the current segment ends here
            should_end_segment = True

        if should_end_segment:
            current_segment_end_id = current_node_id if is_last_node else next_node_id # Ensure correct end for segment

            # Calculate total distance for this segment
            total_segment_distance_units = 0
            start_idx = path_node_ids.index(current_segment_start_id)
            end_idx = path_node_ids.index(current_segment_end_id)
            for j in range(start_idx, end_idx):
                node1_id = path_node_ids[j]
                node2_id = path_node_ids[j+1]
                edge_data = COMPOSITE_GRAPH.get_edge_data(node1_id, node2_id)
                if edge_data and 'weight' in edge_data:
                    total_segment_distance_units += edge_data['weight']
                else:
                    logger.warning(f"Missing weight for edge {node1_id}-{node2_id} in path simplification.")

            segment_type = "segment"
            # Determine segment type more accurately
            if ALL_NODE_ATTRIBUTES[current_segment_start_id]['floor_plan_id'] != ALL_NODE_ATTRIBUTES[current_segment_end_id]['floor_plan_id']:
                segment_type = "inter_floor_transition"
            elif ALL_NODE_ATTRIBUTES[current_segment_end_id]['type'] == 'STAIR':
                segment_type = "stair"
            elif ALL_NODE_ATTRIBUTES[current_segment_end_id]['type'] == 'ELEVATOR':
                segment_type = "elevator"
            elif is_last_node: # The last segment of the entire path
                segment_type = "destination"

            # Add segment to list
            simplified_segments.append({
                "start_node_id": current_segment_start_id,
                "end_node_id": current_segment_end_id,
                "start_coord": (ALL_NODE_ATTRIBUTES[current_segment_start_id]['x_coord'], ALL_NODE_ATTRIBUTES[current_segment_start_id]['y_coord']),
                "end_coord": (ALL_NODE_ATTRIBUTES[current_segment_end_id]['x_coord'], ALL_NODE_ATTRIBUTES[current_segment_end_id]['y_coord']),
                "start_floor_id": ALL_NODE_ATTRIBUTES[current_segment_start_id]['floor_plan_id'],
                "end_floor_id": ALL_NODE_ATTRIBUTES[current_segment_end_id]['floor_plan_id'],
                "distance_meters": round(total_segment_distance_units * DISTANCE_SCALE_FACTOR, 2),
                "type": segment_type
            })

            if not is_last_node:
                current_segment_start_id = current_segment_end_id # Start a new segment from this point

    return simplified_segments


# --- Database Loading and Graph Building Functions ---
def build_composite_graph_from_db() -> Optional[nx.Graph]:
    """Builds a single NetworkX graph containing all floors and inter-floor connections."""
    connection = get_db_connection()
    if not connection:
        return None

    composite_graph = nx.Graph()

    global ALL_NODE_ATTRIBUTES, VENUE_LOOKUP, NODE_ID_TO_COORD, COORD_TO_NODE_ID, STAIR_NODES
    ALL_NODE_ATTRIBUTES.clear()
    VENUE_LOOKUP.clear()
    NODE_ID_TO_COORD.clear()
    COORD_TO_NODE_ID.clear()
    STAIR_NODES.clear()

    try:
        cursor = connection.cursor(dictionary=True)

        # 1. Load all nodes from all floor plans
        logger.info("Loading all nodes from database...")
        query_nodes = """
        SELECT n.id, n.type, n.x_coord, n.y_coord, n.floor_plan_id, n.label,
               v.name as venue_name, v.full_name as venue_full_name, v.imageUrl as venue_image
        FROM node n
        LEFT JOIN venue v ON n.id = v.node_id
        """
        cursor.execute(query_nodes)
        all_nodes_data = cursor.fetchall()

        for row in all_nodes_data:
            node_id = row['id']
            # Round coordinates to avoid floating point issues when using them as dict keys
            coord = (round(row['x_coord'], 2), round(row['y_coord'], 2))

            node_attributes = {
                'id': node_id,
                'type': row['type'],
                'x_coord': row['x_coord'],
                'y_coord': row['y_coord'],
                'floor_plan_id': row['floor_plan_id'],
                'label': row['label'],
                'venue_name': row['venue_name'],
                'venue_full_name': row['venue_full_name'],
                'venue_image': row['venue_image'] if row['venue_image'] is not None else None
            }
            ALL_NODE_ATTRIBUTES[node_id] = node_attributes
            composite_graph.add_node(node_id, **node_attributes)

            NODE_ID_TO_COORD[node_id] = coord
            # This mapping can be ambiguous if (x,y) coords are same on different floors
            # For robustness, avoid using COORD_TO_NODE_ID for node lookup if possible.
            # Instead, rely on node_id directly or VENUE_LOOKUP.
            COORD_TO_NODE_ID[coord] = node_id # Keep for now, but be aware

            if row['venue_name']:
                VENUE_LOOKUP[row['venue_name']] = {
                    'node_id': node_id,
                    'coord': coord,
                    'floor_id': row['floor_plan_id'],
                    'venue_image': row['venue_image'] if row['venue_image'] is not None else None,
                    'venue_full_name': row['venue_full_name']
                }
            if row['type'] == 'STAIR' or row['type'] == 'ELEVATOR':
                STAIR_NODES[node_id] = node_attributes

        logger.info(f"Loaded {len(ALL_NODE_ATTRIBUTES)} total nodes from all floors.")

        # 2. Load all intra-floor edges
        logger.info("Loading all intra-floor edges...")
        query_edges = "SELECT from_node_id, to_node_id, weight FROM edges"
        cursor.execute(query_edges)
        all_edges_data = cursor.fetchall()

        for row in all_edges_data:
            from_node_id = row['from_node_id']
            to_node_id = row['to_node_id']
            weight = float(row['weight'])
            if from_node_id in composite_graph and to_node_id in composite_graph:
                composite_graph.add_edge(from_node_id, to_node_id, weight=weight)
            else:
                logger.warning(f"Skipping intra-floor edge {from_node_id}-{to_node_id}: one or both nodes not found in graph.")

        logger.info(f"Loaded {len(all_edges_data)} intra-floor edges.")

        # 3. Load all inter-floor connections
        logger.info("Loading all inter-floor connections...")
        query_inter_floor = "SELECT node_id_from, node_id_to, weight, type FROM inter_floor_connection"
        cursor.execute(query_inter_floor)
        inter_floor_connections = cursor.fetchall()

        for row in inter_floor_connections:
            from_node_id = row['node_id_from']
            to_node_id = row['node_id_to']
            weight = float(row['weight'])
            conn_type = row['type'] # 'STAIR' or 'ELEVATOR'
            if from_node_id in composite_graph and to_node_id in composite_graph:
                composite_graph.add_edge(from_node_id, to_node_id, weight=weight, connection_type=conn_type)
            else:
                logger.warning(f"Skipping inter-floor connection {from_node_id}-{to_node_id}: one or both nodes not found in graph.")

        logger.info(f"Loaded {len(inter_floor_connections)} inter-floor connections.")

        logger.info(f"Built composite graph with {composite_graph.number_of_nodes()} nodes and {composite_graph.number_of_edges()} edges.")
        return composite_graph

    except Error as e:
        logger.error(f"Error building composite graph from database: {e}")
        return None
    finally:
        if connection.is_connected():
            cursor.close()
            connection.close()

# --- Flask Routes ---
@app.route('/find_path', methods=['POST'])
def find_path_api():
    global COMPOSITE_GRAPH, VENUE_LOOKUP

    if COMPOSITE_GRAPH is None or not VENUE_LOOKUP:
        logger.error("Composite graph data not loaded. Cannot process find_path request.")
        return jsonify({"error": "Server error: Map data not available. Please try again later."}), 503

    if not request.is_json:
        return jsonify({"error": "Invalid request: Content-Type must be application/json"}), 400

    try:
        data = request.get_json()
    except Exception as e:
        logger.warning(f"Could not parse JSON request body: {e}")
        return jsonify({"error": "Invalid JSON format in request body"}), 400

    if not data or 'source' not in data or 'destination' not in data:
        return jsonify({"error": "Missing 'source' or 'destination' in request body. Required format: {'source': 'Venue A', 'destination': 'Venue B'}"}), 400

    source_name = data['source']
    destination_name = data['destination']

    # Find node IDs for the named venues
    if source_name not in VENUE_LOOKUP:
        return jsonify({
            "error": f"Source venue '{source_name}' not found.",
            "available_venues": list(VENUE_LOOKUP.keys())
        }), 404
    
    if destination_name not in VENUE_LOOKUP:
        return jsonify({
            "error": f"Destination venue '{destination_name}' not found.",
            "available_venues": list(VENUE_LOOKUP.keys())
        }), 404

    source_node_id = VENUE_LOOKUP[source_name]['node_id']
    dest_node_id = VENUE_LOOKUP[destination_name]['node_id']

    logger.info(f"Finding path from '{source_name}' (ID: {source_node_id}, Floor: {ALL_NODE_ATTRIBUTES[source_node_id]['floor_plan_id']}) "
                f"to '{destination_name}' (ID: {dest_node_id}, Floor: {ALL_NODE_ATTRIBUTES[dest_node_id]['floor_plan_id']}).")

    path_result_ids = a_star_search(COMPOSITE_GRAPH, source_node_id, dest_node_id)

    if path_result_ids is None:
        return jsonify({
            "error": f"No path found from '{source_name}' to '{destination_name}'.",
            "source_node_id": source_node_id,
            "destination_node_id": dest_node_id
        }), 404
    
    # Simplify the path and get segment distances
    simplified_path_segments = simplify_path(path_result_ids, STRAIGHT_THRESHOLD)

    # Calculate total distance based on the original full path for accuracy
    total_distance_units = 0
    for i in range(len(path_result_ids) - 1):
        edge_data = COMPOSITE_GRAPH.get_edge_data(path_result_ids[i], path_result_ids[i + 1])
        if edge_data:
            total_distance_units += edge_data.get('weight', 0)
    
    total_distance_meters = round(total_distance_units * DISTANCE_SCALE_FACTOR, 2)

    # Format the response for the simplified path segments
    response_segments = []
    for segment in simplified_path_segments:
        # Add a 'description' or 'instruction' for each segment
        instruction = ""
        if segment['type'] == 'inter_floor_transition':
            # Get the connection type from the edge if available
            conn_type = "connection" # Default
            if segment['start_node_id'] in COMPOSITE_GRAPH and segment['end_node_id'] in COMPOSITE_GRAPH:
                edge_attributes = COMPOSITE_GRAPH.get_edge_data(segment['start_node_id'], segment['end_node_id'])
                if edge_attributes and 'connection_type' in edge_attributes:
                    conn_type = edge_attributes['connection_type'].lower() # e.g., 'stair', 'elevator'

            instruction = (f"Proceed via {conn_type} from Floor {segment['start_floor_id']} "
                           f"to Floor {segment['end_floor_id']}.")
        elif segment['type'] == 'stair' or segment['type'] == 'elevator':
            instruction = (f"Head to the {segment['type']} at "
                           f"({segment['end_coord'][0]:.0f},{segment['end_coord'][1]:.0f}) "
                           f"on Floor {segment['end_floor_id']}.")
        elif segment['type'] == 'destination':
            instruction = (f"You have arrived at your destination, {destination_name}, "
                           f"on Floor {segment['end_floor_id']}.")
        else: # Regular segment
            instruction = (f"Walk {segment['distance_meters']:.1f} meters "
                           f"on Floor {segment['start_floor_id']}.")

        response_segments.append({
            "start_node_id": segment['start_node_id'],
            "end_node_id": segment['end_node_id'],
            "start_coord": segment['start_coord'],
            "end_coord": segment['end_coord'],
            "start_floor_id": segment['start_floor_id'],
            "end_floor_id": segment['end_floor_id'],
            "distance_meters": segment['distance_meters'],
            "segment_type": segment['type'], # e.g., "segment", "stair", "inter_floor_transition", "destination"
            "instruction": instruction
        })


    return jsonify({
        "simplified_segments": response_segments,
        "source_node": {
            "id": source_node_id,
            "name": source_name,
            "x_coord": ALL_NODE_ATTRIBUTES[source_node_id]['x_coord'],
            "y_coord": ALL_NODE_ATTRIBUTES[source_node_id]['y_coord'],
            "floor_id": ALL_NODE_ATTRIBUTES[source_node_id]['floor_plan_id']
        },
        "destination_node": {
            "id": dest_node_id,
            "name": destination_name,
            "x_coord": ALL_NODE_ATTRIBUTES[dest_node_id]['x_coord'],
            "y_coord": ALL_NODE_ATTRIBUTES[dest_node_id]['y_coord'],
            "floor_id": ALL_NODE_ATTRIBUTES[dest_node_id]['floor_plan_id']
        },
        "total_distance_meters": total_distance_meters,
        "original_path_length": len(path_result_ids),
        "simplified_segment_count": len(simplified_path_segments)
    })

@app.route('/venues', methods=['GET'])
def get_venues():
    """Get list of all available venues across all floors."""
    global VENUE_LOOKUP, STAIR_NODES

    venues_list = []
    for name, data in VENUE_LOOKUP.items():
        venues_list.append({
            "name": name,
            "full_name": data['venue_full_name'],
            "node_id": data['node_id'],
            "coordinates": data['coord'],
            "floor_id": data['floor_id'],
            "venue_image": data['venue_image']
        })

    stair_nodes_list = []
    for node_id, data in STAIR_NODES.items():
        stair_nodes_list.append({
            "node_id": node_id,
            "coordinates": (data['x_coord'], data['y_coord']),
            "floor_id": data['floor_plan_id'],
            "type": data['type'] # 'STAIR' or 'ELEVATOR'
        })
    return jsonify({"venues": venues_list, "stair_nodes": stair_nodes_list})

# --- Initialization Functions ---
def initialize_map_data() -> bool:
    """Load composite graph data from database."""
    global COMPOSITE_GRAPH

    logger.info("Initializing composite map data for all floors...")
    COMPOSITE_GRAPH = build_composite_graph_from_db()

    if COMPOSITE_GRAPH is None:
        logger.critical("Composite graph could not be built from database. Pathfinding service will not be operational.")
        return False

    logger.info(f"Composite map data initialization complete. Graph has {COMPOSITE_GRAPH.number_of_nodes()} nodes, {len(VENUE_LOOKUP)} venues loaded across all floors.")
    return True

# --- Main Execution ---
if __name__ == '__main__':
    # Initialize map data for the Flask app
    initialize_map_data()

    # Run Flask app
    flask_host = get_config_value('Flask', 'Host', '0.0.0.0')
    flask_port = get_config_value('Flask', 'Port', 8000, 'int')
    flask_debug_mode = get_config_value('Flask', 'DebugMode', False, 'boolean')

    logger.info(f"Starting Flask app on {flask_host}:{flask_port} (Debug: {flask_debug_mode})")
    app.run(host=flask_host, port=flask_port, debug=flask_debug_mode)
else:
    # Initialize when imported by WSGI server
    if get_config_value('Server', 'AutoInitialize', True, 'boolean'):
        initialize_map_data()