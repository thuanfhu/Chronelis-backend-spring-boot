package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.team.AddTeamMemberRequest;
import com.devloopsx.chronelis.dto.request.team.CreateWorkspaceTeamRequest;
import com.devloopsx.chronelis.dto.request.team.UpdateWorkspaceTeamRequest;
import com.devloopsx.chronelis.dto.response.team.WorkspaceTeamMemberResponse;
import com.devloopsx.chronelis.dto.response.team.WorkspaceTeamResponse;
import java.util.List;

public interface WorkspaceTeamService {
  WorkspaceTeamResponse createTeam(CreateWorkspaceTeamRequest request);

  WorkspaceTeamResponse updateTeam(Long teamId, UpdateWorkspaceTeamRequest request);

  List<WorkspaceTeamResponse> listByWorkspace(Long workspaceId);

  WorkspaceTeamResponse getTeam(Long teamId);

  void deleteTeam(Long teamId);

  WorkspaceTeamMemberResponse addMember(Long teamId, AddTeamMemberRequest request);

  void removeMember(Long teamId, String userId);

  List<WorkspaceTeamMemberResponse> listMembers(Long teamId);
}
