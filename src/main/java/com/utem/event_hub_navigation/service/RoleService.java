package com.utem.event_hub_navigation.service;

import java.util.List;

import com.utem.event_hub_navigation.model.Role;

public interface RoleService {

    // add role
    Role addRole(Role role);

    // remove role
    void removeRole(Integer id);

    // get role by id
    Role getRoleById(Integer id);

    // get all roles
    List<Role> getAllRoles();

    List<Role> getRolesByName(String name);

    void deleteRole(Integer id);

}