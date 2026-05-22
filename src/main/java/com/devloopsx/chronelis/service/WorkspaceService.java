package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.workspace.AddWorkspaceMemberRequest;
import com.devloopsx.chronelis.dto.request.workspace.CreateWorkspaceRequest;
import com.devloopsx.chronelis.dto.request.workspace.UpdateWorkspaceMemberRoleRequest;
import com.devloopsx.chronelis.dto.request.workspace.UpdateWorkspaceRequest;
import com.devloopsx.chronelis.dto.response.common.PaginationResponse;
import com.devloopsx.chronelis.dto.response.workspace.WorkspaceMemberResponse;
import com.devloopsx.chronelis.dto.response.workspace.WorkspaceResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface WorkspaceService {
  WorkspaceResponse createWorkspace(CreateWorkspaceRequest request);

  WorkspaceResponse updateWorkspace(Long workspaceId, UpdateWorkspaceRequest request);

  WorkspaceResponse getWorkspace(Long workspaceId);

  PaginationResponse listVisibleWorkspaces(Pageable pageable);

  WorkspaceMemberResponse addMember(Long workspaceId, AddWorkspaceMemberRequest request);

  List<WorkspaceMemberResponse> listMembers(Long workspaceId);

  WorkspaceMemberResponse updateMemberRole(
      Long workspaceId, String userId, UpdateWorkspaceMemberRoleRequest request);

  void removeMember(Long workspaceId, String userId);

  void deleteWorkspace(Long workspaceId);
}
