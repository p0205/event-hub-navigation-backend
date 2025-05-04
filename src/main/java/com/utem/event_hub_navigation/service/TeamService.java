package com.utem.event_hub_navigation.service;

import java.util.List;

import com.utem.event_hub_navigation.dto.TeamMemberDTO;

public interface TeamService {


    public void addTeamMember(Integer eventId, Integer userId, Integer roleId) throws Exception;

    public List<TeamMemberDTO> getTeamMembers(Integer eventId);

    public void addTeamMembers(Integer eventId, List<Integer> userId, Integer roleId)throws Exception;

    public void removeTeamMember(Integer eventId, Integer userId);

}