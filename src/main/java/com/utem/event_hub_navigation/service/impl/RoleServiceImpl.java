package com.utem.event_hub_navigation.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.model.Role;
import com.utem.event_hub_navigation.repo.RoleRepo;
import com.utem.event_hub_navigation.service.RoleService;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepo roleRepo;

    @Autowired
    public RoleServiceImpl(RoleRepo roleRepo) {
        this.roleRepo = roleRepo;
    }

    // add role
    @Override
    public Role addRole(Role role) {
        // Check if the role already exists
        if (roleRepo.existsByName(role.getName())) {
            throw new IllegalArgumentException("Role " + role.getName() + " already exists.");
        }
        return roleRepo.save(role);
    }
    // remove role
    @Override
    public void removeRole(Integer id) {
        roleRepo.deleteById(id);
    }
    // get role by id
    @Override
    public Role getRoleById(Integer id) {
        return roleRepo.findById(id).orElse(null);
    }
    // get all roles
    @Override
    public List<Role> getAllRoles() {
        return roleRepo.findAll();
    }

    @Override
    public List<Role> getRolesByName(String name) {
       return roleRepo.findByNameContains(name);
    }

    @Override
    public void deleteRole(Integer id) {
        roleRepo.deleteById(id);
    }
}
