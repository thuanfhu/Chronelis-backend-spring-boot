package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.invite.CreateWorkspaceInviteRequest;
import com.devloopsx.chronelis.dto.request.invite.JoinByInviteRequest;
import com.devloopsx.chronelis.dto.response.invite.WorkspaceInviteResponse;
import java.util.List;

public interface WorkspaceInviteService {
  WorkspaceInviteResponse createInvite(CreateWorkspaceInviteRequest request);

  List<WorkspaceInviteResponse> listActiveInvites(Long workspaceId);

  void revokeInvite(Long inviteId);

  WorkspaceInviteResponse validateInviteCode(String inviteCode);

  void joinByInvite(JoinByInviteRequest request);
}
