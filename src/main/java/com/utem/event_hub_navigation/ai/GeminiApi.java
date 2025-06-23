// Required dependencies:
// implementation 'com.google.genai:client:0.1.0' (or latest)
// implementation 'com.google.guava:guava:32.1.2-jre' (or latest)
// Ensure these are present in your build.gradle or pom.xml

package com.utem.event_hub_navigation.ai;

import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.ThinkingConfig;
import com.utem.event_hub_navigation.dto.EventArticleDTO;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class GeminiApi {

    private final GeminiApiKeyProvider apiKeyProvider;

    @Autowired
    public GeminiApi(GeminiApiKeyProvider apiKeyProvider) {
        this.apiKeyProvider = apiKeyProvider;
    }

    public String generatePostEventArticle(EventArticleDTO dto) {

        Client client = Client.builder().apiKey(apiKeyProvider.getApiKey()).build();

        // You can set language dynamically or default to English
        // String language = "English"; // Or "Bahasa Melayu" if needed

        StringBuilder prompt = new StringBuilder();
        prompt.append("# Comprehensive AI Bulletin Article Generator Prompt\n\n");
        prompt.append("## System Instructions\n");
        prompt.append("You are an expert bulletin article writer specializing in academic event reporting for Malaysian universities. Your task is to generate comprehensive, professional bulletin articles that showcase successful event execution and highlight key achievements. You must write in the specified language (English or Bahasa Melayu) with appropriate academic tone and cultural context.\n\n");
        prompt.append("## Language Instructions\n\n");

        prompt.append("**If user specifies English:**\n");
        prompt.append("- Use formal academic English appropriate for university publications\n");
        prompt.append("- Include Malaysian English conventions where culturally relevant\n");
        prompt.append("- Maintain professional tone throughout\n");
        prompt.append("- Use active voice where possible\n\n");
        prompt.append("**If user specifies Bahasa Melayu:**\n");
        prompt.append("- Use formal Bahasa Melayu suitable for official university communications\n");
        prompt.append("- Follow standard academic writing conventions in Bahasa Melayu\n");
        prompt.append("- Use appropriate honorifics and formal expressions\n");
        prompt.append("- Ensure grammatically correct sentence structures\n\n");

        // prompt.append("## Language Selection: ").append(dto.getLanguage()).append("\n");;

        
        prompt.append("## Content Generation Guidelines\n\n");
        prompt.append("### 1. Article Structure Flexibility\n");
        prompt.append("- Do not follow rigid paragraph templates\n");
        prompt.append("- Create natural flow based on available information\n");
        prompt.append("- Prioritize most impactful information first\n");
        prompt.append("- Adapt length and detail based on data richness\n");
        prompt.append("- Write as a cohesive narrative, not template filling\n\n");
        prompt.append("### 2. Data Utilization Hierarchy\n");
        prompt.append("**Primary Database Sources (Use First):**\n");
        prompt.append("- University: UTeM\n");
        prompt.append("- Event details (name, dates, type, description)\n");
        prompt.append("- Session information (names, venues, timing)\n");
        prompt.append("- Attendance metrics (registration vs actual attendance)\n");
        prompt.append("- Participant demographics (faculty, year, gender distribution)\n");
        prompt.append("- Feedback ratings and comments analysis\n");
        prompt.append("- Team member roles and contributions\n");
        prompt.append("- Venue utilization across sessions\n\n");
        prompt.append("**Secondary Information (Use if Available):**\n");
        prompt.append("- Budget allocation and expenditure\n");
        prompt.append("- Media documentation\n");
        prompt.append("- Supporting documents\n");
        prompt.append("- Inter-departmental collaboration\n\n");
        prompt.append("**Manual Input Integration:**\n");
        prompt.append("- Seamlessly weave manually provided context\n");
        prompt.append("- Prioritize strategic objectives and outcomes\n");
        prompt.append("- Include specific learning achievements\n");
        prompt.append("- Highlight future implications and opportunities\n\n");
        prompt.append("### 3. Content Depth Requirements\n");
        prompt.append("**Event Context:**\n");
        prompt.append("- Establish significance within university/faculty context\n");
        prompt.append("- Connect to broader educational objectives\n");
        prompt.append("- Highlight innovation or unique aspects\n");
        prompt.append("- Position within current academic landscape\n\n");
        prompt.append("**Execution Excellence:**\n");
        prompt.append("- Demonstrate thorough planning and organization\n");
        prompt.append("- Showcase multi-session coordination\n");
        prompt.append("- Highlight venue management efficiency\n");
        prompt.append("- Emphasize attendance success rates\n\n");
        prompt.append("**Impact Documentation:**\n");
        prompt.append("- Quantify participation metrics meaningfully\n");
        prompt.append("- Analyze feedback patterns for insights\n");
        prompt.append("- Connect activities to learning outcomes\n");
        prompt.append("- Project long-term benefits and applications\n\n");
        prompt.append("**Recognition and Appreciation:**\n");
        prompt.append("- Acknowledge team contributions with specific roles\n");
        prompt.append("- Recognize institutional support appropriately\n");
        prompt.append("- Thank collaborative partners and stakeholders\n");
        prompt.append("- Credit student participation and engagement\n\n");
        prompt.append("### 4. Data Interpretation Guidelines\n");
        prompt.append("**Attendance Analysis:**\n");
        prompt.append("- Calculate and present attendance rates professionally\n");
        prompt.append("- Compare registration vs actual participation\n");
        prompt.append("- Analyze session-by-session engagement patterns\n");
        prompt.append("- Highlight peak participation moments\n\n");
        prompt.append("**Demographic Insights:**\n");
        prompt.append("- Present participant distribution meaningfully\n");
        prompt.append("- Identify cross-faculty collaboration\n");
        prompt.append("- Highlight diverse year-level engagement\n");
        prompt.append("- Note gender participation balance where relevant\n\n");
        prompt.append("**Feedback Synthesis:**\n");
        prompt.append("- Convert numerical ratings to qualitative assessments\n");
        prompt.append("- Extract key themes from comment analysis\n");
        prompt.append("- Identify areas of exceptional success\n");
        prompt.append("- Present constructive insights professionally\n\n");
        prompt.append("**Session Coordination:**\n");
        prompt.append("- Demonstrate complex event management\n");
        prompt.append("- Highlight multi-venue coordination success\n");
        prompt.append("- Showcase timing efficiency and logistics\n");
        prompt.append("- Emphasize participant journey continuity\n\n");
        prompt.append("### 5. Writing Quality Standards\n");
        prompt.append("**Professional Tone:**\n");
        prompt.append("- Maintain scholarly yet accessible language\n");
        prompt.append("- Balance formality with engagement\n");
        prompt.append("- Use confident, achievement-focused phrasing\n");
        prompt.append("- Avoid excessive jargon or technical terms\n\n");
        prompt.append("**Factual Accuracy:**\n");
        prompt.append("- Present all database-derived information precisely\n");
        prompt.append("- Clearly distinguish between data and interpretation\n");
        prompt.append("- Avoid speculation beyond available evidence\n");
        prompt.append("- Maintain consistency in facts throughout\n\n");
        prompt.append("**Engagement Techniques:**\n");
        prompt.append("- Use varied sentence structures for readability\n");
        prompt.append("- Include specific examples and concrete details\n");
        prompt.append("- Create logical flow between ideas\n");
        prompt.append("- End with forward-looking statements\n\n");
        prompt.append("**Cultural Sensitivity:**\n");
        prompt.append("- Respect Malaysian academic conventions\n");
        prompt.append("- Use appropriate institutional terminology\n");
        prompt.append("- Acknowledge hierarchical relationships properly\n");
        prompt.append("- Include relevant cultural context where applicable\n\n");
        prompt.append("### 6. Missing Information Protocol\n");
        prompt.append("**When Database Information is Incomplete:**\n");
        prompt.append("- SKIP AND IGNORE THE INFORAMTION\n");
        prompt.append("**For Strategic Context:**\n");
        prompt.append("- Request specific program objectives if not provided\n");
        prompt.append("- Ask for key learning outcomes achieved\n");
        prompt.append("- Seek future program directions or implications\n");
        prompt.append("- Inquire about unique program innovations\n\n");
        prompt.append("### 7. Output Specifications\n");
        prompt.append("**Article Length:** \n");
        prompt.append("- Comprehensive articles (400-800 words typical)\n");
        prompt.append("- Adapt length to information richness\n");
        prompt.append("- Prioritize quality over word count\n");
        prompt.append("- Ensure all key information is included\n\n");

        prompt.append("**Formatting:**\n");
        prompt.append("- Use event name as title\n");
        prompt.append("- Important: Response in PLAIN Text\n");
        prompt.append("- Include byline with organizers and advisors\n");
        prompt.append("- Create natural paragraph breaks\n");

        prompt.append("**Final Review:**\n");
        prompt.append("- Ensure factual consistency throughout\n");
        prompt.append("- Verify language appropriateness for target audience\n");
        prompt.append("- Confirm all database information is accurately represented\n");
        prompt.append("- Check that manual input is seamlessly integrated\n\n");
        prompt.append("## Generation Protocol\n\n");
        prompt.append("1. **Analyze Provided Data:** Review all database information and manual inputs\n");
        prompt.append("2. **Determine Language:** Confirm English or Bahasa Melayu requirement\n");
        prompt.append("3. **Assess Information Richness:** Identify strongest data points for emphasis\n");
        prompt.append("4. **Create Narrative Flow:** Develop logical progression of ideas\n");
        prompt.append("5. **Write Comprehensively:** Generate full article without template constraints\n");
        prompt.append("6. **Integrate Seamlessly:** Blend database facts with contextual information\n");
        prompt.append("7. **Conclude Meaningfully:** End with impact statement and future outlook\n\n");
        prompt.append("- Important: Response in PLAIN Text\n");
        prompt.append("**Remember:** Your goal is to create a comprehensive, professional bulletin article that celebrates achievement while accurately reflecting the event's success through available data. Write naturally and engagingly, letting the information guide the structure rather than forcing it into predetermined templates.\n\n");
        prompt.append("## Language: ").append(dto.getManualInputs().getLanguage()).append("\n\n");
        prompt.append("## Event Data and Manual Inputs\n");
        prompt.append("Event Name: ").append(dto.getEventName()).append("\n");
        prompt.append("Event Type: ").append(dto.getEventType()).append("\n");
        prompt.append("Start Date: ").append(dto.getStartDateTime()).append("\n");
        prompt.append("End Date: ").append(dto.getEndDateTime()).append("\n");
        prompt.append("Total Participants: ").append(dto.getParticipantsNo()).append("\n");
        prompt.append("Detailed Session & Venue Info: ").append(dto.getSessionVenueSummaries()).append("\n");
        dto.getSessions().forEach(session -> {
            prompt.append("Session Name: ").append(session.getSessionName()).append("\n");
            prompt.append("Venue: ");
            for (var venue : session.getVenues()) {
                prompt.append(venue.getName()).append(") ");
            }
            prompt.append("\n");
            prompt.append("Start Time: ").append(session.getStartDateTime()).append("\n");
            prompt.append("End Time: ").append(session.getEndDateTime()).append("\n");
           
        });
        prompt.append("Media URLs: ").append(dto.getMediaUrls()).append("\n");
        prompt.append("Committee/Contributors: ").append(dto.getCommitteeNames()).append("\n");
        prompt.append("Organizing Body (manual): ").append(dto.getManualInputs().getOrganizingBody()).append("\n");
        prompt.append("Credit Individuals (manual): ").append(dto.getManualInputs().getCreditIndividuals()).append("\n");
        prompt.append("Event Objectives (manual): ").append(dto.getManualInputs().getEventObjectives()).append("\n");
        prompt.append("Activities Conducted (manual): ").append(dto.getManualInputs().getActivitiesConducted()).append("\n");
        prompt.append("Target Audience (manual): ").append(dto.getManualInputs().getTargetAudience()).append("\n");
        prompt.append("Perceived Impact (manual): ").append(dto.getManualInputs().getPerceivedImpact()).append("\n");
        prompt.append("Acknowledgements (manual): ").append(dto.getManualInputs().getAcknowledgements()).append("\n");
        prompt.append("Appreciation Message (manual): ").append(dto.getManualInputs().getAppreciationMessage()).append("\n");
        // Add more fields as needed

        String model = "gemini-2.5-flash";
        List<Content> contents = ImmutableList.of(
                Content.builder()
                        .role("user")
                        .parts(ImmutableList.of(
                                Part.fromText(prompt.toString())))
                        .build());
        GenerateContentConfig config = GenerateContentConfig
                .builder()
                .thinkingConfig(
                        ThinkingConfig
                                .builder()
                                .thinkingBudget(-1)
                                .build())
                .responseMimeType("text/plain")
                .build();

        ResponseStream<GenerateContentResponse> responseStream = client.models.generateContentStream(model, contents,
                config);
        StringBuilder result = new StringBuilder();
        for (GenerateContentResponse res : responseStream) {
            if (res.candidates().isEmpty() || res.candidates().get().get(0).content().isEmpty()
                    || res.candidates().get().get(0).content().get().parts().isEmpty()) {
                continue;
            }
            List<Part> parts = res.candidates().get().get(0).content().get().parts().get();
            for (Part part : parts) {
                part.text().ifPresent(result::append);
            }
        }
        responseStream.close();
        return result.toString();
    }
}

@Component
class GeminiApiKeyProvider {
    @Value("${gemini.api.key}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}