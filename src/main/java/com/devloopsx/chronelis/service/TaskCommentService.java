package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.dto.request.taskcomment.CreateTaskCommentRequest;
import com.devloopsx.chronelis.dto.request.taskcomment.UpdateTaskCommentRequest;
import com.devloopsx.chronelis.dto.response.taskcomment.TaskCommentResponse;
import java.util.List;

public interface TaskCommentService {
  TaskCommentResponse addComment(CreateTaskCommentRequest request);

  TaskCommentResponse updateComment(Long commentId, UpdateTaskCommentRequest request);

  void deleteComment(Long commentId);

  List<TaskCommentResponse> listCommentsByTask(Long taskId);
}
