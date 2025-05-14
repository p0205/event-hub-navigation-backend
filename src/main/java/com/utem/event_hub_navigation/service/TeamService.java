package com.utem.event_hub_navigation.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.utem.event_hub_navigation.dto.TeamMemberDTO;

public interface TeamService {


    public void addTeamMemberRole(Integer eventId, Integer userId, Integer roleId) throws Exception;

    public Page<TeamMemberDTO> getTeamMembers(Integer eventId,Pageable pageable);

    public void addTeamMembers(Integer eventId, List<Integer> userId, Integer roleId)throws Exception;

    public void removeTeamMember(Integer eventId, Integer userId);

}