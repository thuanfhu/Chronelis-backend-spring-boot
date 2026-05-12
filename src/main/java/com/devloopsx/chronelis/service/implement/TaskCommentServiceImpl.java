package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.*;
import com.devloopsx.chronelis.domain.Task;
import com.devloopsx.chronelis.domain.TaskComment;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.dto.request.taskcomment.CreateTaskCommentRequest;
import com.devloopsx.chronelis.dto.request.taskcomment.UpdateTaskCommentRequest;
import com.devloopsx.chronelis.dto.response.taskcomment.TaskCommentResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.TaskCommentMapper;
import com.devloopsx.chronelis.repository.TaskCommentRepository;
import com.devloopsx.chronelis.service.*;
import com.devloopsx.chronelis.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskCommentServiceImpl implements TaskCommentService {
        TaskCommentRepository taskCommentRepository;
        TaskCommentMapper taskCommentMapper;
        CollaborationAccessService collaborationAccessService;
        ProjectPermissionService projectPermissionService;
        SecurityUtils securityUtils;
        NotificationService notificationService;
        ActivityLogService activityLogService;
        RealtimeEventPublisherService realtimeEventPublisherService;

        @Override
        @Transactional
        public TaskCommentResponse addComment(CreateTaskCommentRequest request) {
                Task task = collaborationAccessService.requireTask(request.getTaskId());
                collaborationAccessService.ensureCurrentUserCanContributeToProject(task.getProject().getId());

                User currentUser = securityUtils.getAuthenticatedUser();
                LocalDateTime now = LocalDateTime.now();

                TaskComment taskComment = taskCommentMapper.toEntity(request);
                taskComment.setTask(task);
                taskComment.setUser(currentUser);

                if (request.getParentCommentId() != null) {
                        TaskComment parentComment = taskCommentRepository.findById(request.getParentCommentId())
                                        .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                                                        "Comment cha không tồn tại"));

                        if (!parentComment.getTask().getId().equals(task.getId())) {
                                throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                                                "Comment cha không thuộc task được chọn");
                        }

                        if (parentComment.getParentComment() != null) {
                                throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                                                "Chỉ hỗ trợ trả lời một cấp");
                        }

                        taskComment.setParentComment(parentComment);
                }

                taskComment.setCreatedAt(now);
                taskComment.setUpdatedAt(now);

                TaskComment savedComment = taskCommentRepository.save(taskComment);

                activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                                ActivityActionType.COMMENT_ADDED, ActivityTargetType.COMMENT, savedComment.getId(),
                                "Thêm bình luận cho task " + task.getTitle());

                notifyCommentParticipants(task, currentUser, savedComment.getId());

                TaskCommentResponse response = taskCommentMapper.toResponse(savedComment);
                realtimeEventPublisherService.publishTaskEvent(task.getProject().getWorkspace().getId(),
                                task.getProject().getId(),
                                task.getId(), "task-comment.added", response);
                return response;
        }

        @Override
        @Transactional
        public TaskCommentResponse updateComment(Long commentId, UpdateTaskCommentRequest request) {
                TaskComment comment = taskCommentRepository.findById(commentId)
                                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND,
                                                "Comment không tồn tại"));

                Task task = comment.getTask();
                collaborationAccessService.ensureCurrentUserCanContributeToProject(task.getProject().getId());

                User currentUser = securityUtils.getAuthenticatedUser();
                if (!canModifyComment(comment, currentUser)) {
                        throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS,
                                        "Bạn không có quyền sửa bình luận này");
                }

                comment.setContent(request.getContent());
                comment.setUpdatedAt(LocalDateTime.now());

                TaskComment updatedComment = taskCommentRepository.save(comment);

                activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                                ActivityActionType.COMMENT_UPDATED, ActivityTargetType.COMMENT, commentId,
                                "Cập nhật bình luận của task " + task.getTitle());

                TaskCommentResponse response = taskCommentMapper.toResponse(updatedComment);
                realtimeEventPublisherService.publishTaskEvent(task.getProject().getWorkspace().getId(),
                                task.getProject().getId(),
                                task.getId(), "task-comment.updated", response);
                return response;
        }

        @Override
        @Transactional
        public void deleteComment(Long commentId) {
                TaskComment comment = taskCommentRepository.findById(commentId)
                                .orElse(null);

                if (comment == null) {
                        return;
                }

                Task task = comment.getTask();
                collaborationAccessService.ensureCurrentUserCanContributeToProject(task.getProject().getId());

                User currentUser = securityUtils.getAuthenticatedUser();
                if (!canModifyComment(comment, currentUser)) {
                        throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS,
                                        "Bạn không có quyền xóa bình luận này");
                }

                int deletedRows = taskCommentRepository.deleteByIdDirect(commentId);
                if (deletedRows == 0) {
                        return;
                }

                activityLogService.createLog(task.getProject().getWorkspace().getId(), currentUser.getUserId(),
                                ActivityActionType.COMMENT_DELETED, ActivityTargetType.COMMENT, commentId,
                                "Xóa bình luận của task " + task.getTitle());

                realtimeEventPublisherService.publishTaskEvent(task.getProject().getWorkspace().getId(),
                                task.getProject().getId(),
                                task.getId(), "task-comment.deleted", commentId);
        }

        @Override
        public List<TaskCommentResponse> listCommentsByTask(Long taskId) {
                Task task = collaborationAccessService.requireTask(taskId);
                collaborationAccessService.ensureCurrentUserCanAccessProject(task.getProject().getId());

                return taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(taskId).stream()
                                .map(taskCommentMapper::toResponse)
                                .toList();
        }

        private boolean canModifyComment(TaskComment comment, User currentUser) {
                if (comment.getUser().getUserId().equals(currentUser.getUserId())) {
                        return true;
                }

                return projectPermissionService.resolveCurrentUserRole(comment.getTask().getProject())
                                .atLeast(EffectiveProjectAccessRoleType.MANAGER);
        }

        private void notifyCommentParticipants(Task task, User actor, Long commentId) {
                Set<String> recipients = new HashSet<>();

                if (task.getAssignee() != null) {
                        recipients.add(task.getAssignee().getUserId());
                }
                if (task.getCreatedBy() != null) {
                        recipients.add(task.getCreatedBy().getUserId());
                }

        recipients.remove(actor.getUserId());
        recipients.retainAll(projectPermissionService.findAuthorizedUserIds(task.getProject()));

        for (String recipientId : recipients) {
            notificationService.createAndPublish(recipientId, NotificationType.TASK_COMMENTED,
                                        "Có bình luận mới", actor.getEmail() + " vừa bình luận task " + task.getTitle(),
                                        ReferenceType.COMMENT, commentId);
                }
        }
}
