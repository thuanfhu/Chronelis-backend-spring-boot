package com.devloopsx.chronelis.configuration;

import com.devloopsx.chronelis.constant.*;
import com.devloopsx.chronelis.domain.*;
import com.devloopsx.chronelis.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Order(2) // Run after DataInitializer
public class DatabaseSeeder implements ApplicationRunner {
    PasswordEncoder passwordEncoder;
    PlatformTransactionManager transactionManager;

    UserRepository userRepository;
    RoleRepository roleRepository;
    WorkspaceRepository workspaceRepository;
    WorkspaceMemberRepository workspaceMemberRepository;
    ProjectRepository projectRepository;
    GoalRepository goalRepository;
    TaskStatusRepository taskStatusRepository;
    TaskTypeRepository taskTypeRepository;
    TaskRepository taskRepository;
    TaskScheduleRepository taskScheduleRepository;
    TaskCommentRepository taskCommentRepository;
    TaskCheckItemRepository taskCheckItemRepository;
    WorkspaceTeamRepository workspaceTeamRepository;
    WorkspaceTeamMemberRepository workspaceTeamMemberRepository;

    @NonFinal
    @Value("${chronelis.allowed-init}")
    protected Boolean ALLOWED_INIT;

    @NonFinal
    @Value("${chronelis.account.base-password}")
    protected String BASE_PASSWORD;

    // ─── Seed templates ───

    static final String[] WORKSPACE_NAMES = {
            "Dev Team Alpha", "Marketing HQ", "Product Design Lab", "QA & Testing",
            "Customer Success", "Engineering Core", "Data Analytics", "Growth Team",
            "Mobile Squad", "DevOps Infrastructure", "Content Strategy", "Sales Operations",
            "HR & Culture", "Finance Team", "Research Lab", "Platform Engineering",
            "Security Team", "AI & ML Team", "Cloud Services", "Frontend Guild",
            "Backend Guild", "UX Research", "Technical Writing", "Support Team",
            "Innovation Hub"
    };

    static final String[][] PROJECT_TEMPLATES = {
            { "Website Redesign", "Thiết kế lại giao diện website chính" },
            { "Mobile App v2", "Phát triển ứng dụng di động phiên bản 2" },
            { "API Platform", "Xây dựng nền tảng API cho hệ thống" },
            { "Dashboard Analytics", "Bảng phân tích dữ liệu cho quản lý" },
            { "Customer Portal", "Cổng thông tin khách hàng" },
            { "E-commerce Module", "Module thương mại điện tử" },
            { "CI/CD Pipeline", "Xây dựng pipeline CI/CD tự động" },
            { "Documentation Hub", "Trung tâm tài liệu dự án" },
            { "Performance Optimization", "Tối ưu hiệu suất hệ thống" },
            { "Security Audit", "Kiểm tra và nâng cấp bảo mật" },
            { "Data Migration", "Di chuyển và chuyển đổi dữ liệu" },
            { "User Research", "Nghiên cứu người dùng và UX" },
            { "Marketing Automation", "Tự động hóa marketing" },
            { "Payment Integration", "Tích hợp cổng thanh toán" },
            { "Notification System", "Hệ thống thông báo đa kênh" },
            { "Search Engine", "Xây dựng công cụ tìm kiếm nội bộ" },
            { "Chat System", "Hệ thống chat real-time" },
            { "Report Generator", "Công cụ tạo báo cáo tự động" },
            { "Inventory Management", "Quản lý kho hàng" },
            { "HR Management", "Hệ thống quản lý nhân sự" },
    };

    static final String[][] STATUS_TEMPLATES = {
            { "Backlog", "BACKLOG", "false" },
            { "To Do", "TODO", "false" },
            { "In Progress", "IN_PROGRESS", "false" },
            { "Review", "REVIEW", "false" },
            { "Done", "DONE", "true" },
    };

    static final String[][] TASK_TYPE_TEMPLATES = {
            { "Bug", "Lỗi cần sửa", "#EF4444", "bug" },
            { "Feature", "Tính năng mới", "#3B82F6", "sparkles" },
            { "Improvement", "Cải tiến", "#8B5CF6", "zap" },
            { "Documentation", "Tài liệu", "#10B981", "book" },
            { "Research", "Nghiên cứu", "#F59E0B", "search" },
    };

    static final String[] GOAL_TITLES = {
            "Hoàn thành thiết kế UI/UX", "Xây dựng Backend API", "Viết Unit Tests",
            "Tích hợp CI/CD", "Triển khai Production", "Tối ưu Performance",
            "Viết tài liệu kỹ thuật", "Review và Refactor code", "Testing E2E",
            "Setup môi trường Dev", "Phân tích yêu cầu", "Thiết kế Database",
            "Implement Authentication", "Build Admin Dashboard", "Mobile Responsive",
            "API Documentation", "Load Testing", "Security Review",
            "User Acceptance Testing", "Launch Preparation",
    };

    static final String[] TASK_TITLES = {
            "Thiết kế trang chủ", "Implement login/register", "Tạo API endpoint users",
            "Viết test cho service layer", "Setup Docker compose", "Design database schema",
            "Code review pull request", "Fix lỗi hiển thị mobile", "Tối ưu query database",
            "Cập nhật API documentation", "Implement search feature", "Add pagination",
            "Setup logging system", "Create email templates", "Implement file upload",
            "Add input validation", "Create admin panel", "Setup monitoring",
            "Fix security vulnerabilities", "Implement caching layer", "Create data export",
            "Add notification system", "Optimize image loading", "Setup error tracking",
            "Implement WebSocket", "Add role-based access", "Create dashboard widgets",
            "Fix CSS responsiveness", "Add dark mode", "Implement filters",
            "Create onboarding flow", "Setup backup system", "Add rate limiting",
            "Implement undo/redo", "Create settings page", "Add multi-language support",
            "Optimize bundle size", "Setup CDN", "Create API gateway",
            "Implement real-time sync", "Build notification center",
    };

    static final String[] COMMENT_TEMPLATES = {
            "Đã xem xét, cần sửa thêm một số chỗ",
            "LGTM! Có thể merge được rồi",
            "Cần thêm test case cho edge case này",
            "Đã deploy lên staging, cần QA kiểm tra",
            "Tiến độ tốt, tiếp tục phát huy",
            "Cần thảo luận thêm về approach này",
            "Đã fix theo review, xin review lại",
            "Thêm documentation cho phần này nhé",
            "Performance cần cải thiện ở query này",
            "UI đẹp rồi, chỉ cần chỉnh responsive",
    };

    static final String[] CHECK_ITEM_TITLES = {
            "Viết unit test", "Code review", "Update documentation",
            "Test trên mobile", "Kiểm tra performance", "Fix lint warnings",
            "Add error handling", "Test edge cases", "Update changelog",
            "Deploy staging", "QA testing", "Update API docs",
            "Check security", "Optimize queries", "Add logging",
    };

    static final String[] TEAM_NAMES = {
            "Frontend", "Backend", "Full Stack", "DevOps", "QA",
            "Design", "Product", "Data", "Security", "Mobile",
    };

    static final int TARGET_USER_COUNT = 100;
    static final Pattern NON_ASCII_WORD = Pattern.compile("[^a-z0-9]");

    // ─── Runner ───

    @Override
    public void run(ApplicationArguments args) {
        if (!ALLOWED_INIT) {
            log.info(">>> SKIP SEED DATA - ALLOWED_INIT is false");
            return;
        }
        if (workspaceRepository.count() > 0) {
            log.info(">>> SKIP SEED DATA - workspaces already exist");
            return;
        }

        log.info(">>> START SEEDING DEMO DATA");
        // Each step runs in its own short transaction to avoid long-running write-locks
        // that block concurrent login requests during seeding.
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        Faker faker = new Faker(Locale.of("vi"));
        Random random = new Random(42);
        LocalDateTime now = LocalDateTime.now();

        List<User> users = tx.execute(s -> seedUsers(faker, random));
        List<Workspace> workspaces = tx.execute(s -> seedWorkspaces(random, users, now));
        Map<Long, List<User>> wsMembersMap = tx.execute(s -> seedWorkspaceMembers(random, workspaces, users));
        List<Project> projects = tx.execute(s -> seedProjects(random, workspaces, wsMembersMap, now));
        Map<Long, List<TaskStatus>> statusesMap = tx.execute(s -> seedTaskStatuses(projects));
        Map<Long, List<TaskType>> taskTypesMap = tx.execute(s -> seedTaskTypes(projects, workspaces, random));
        Map<Long, List<Goal>> goalsMap = tx.execute(s -> seedGoals(random, projects, workspaces, wsMembersMap, now));
        List<Task> tasks = tx.execute(s -> seedTasks(faker, random, projects, goalsMap, statusesMap, taskTypesMap,
                workspaces, wsMembersMap, now));
        tx.executeWithoutResult(s -> seedTaskSchedules(random, tasks, projects, workspaces, wsMembersMap, now));
        tx.executeWithoutResult(s -> seedTaskComments(random, tasks, projects, workspaces, wsMembersMap));
        tx.executeWithoutResult(s -> seedTaskCheckItems(random, tasks));
        tx.executeWithoutResult(s -> seedWorkspaceTeams(random, workspaces, wsMembersMap));

        log.info(">>> END SEEDING DEMO DATA");
    }

    // ─── Seed methods ───

    private List<User> seedUsers(Faker faker, Random random) {
        Role userRole = roleRepository.findByName(RoleType.USER_ROLE.getName())
                .orElseThrow(() -> new RuntimeException("USER role not found — run DataInitializer first"));

        String encodedPassword = passwordEncoder.encode(BASE_PASSWORD);
        List<User> existingUsers = new ArrayList<>(userRepository.findAll());
        existingUsers.sort(Comparator.comparing(User::getEmail));

        Set<String> usedEmails = new HashSet<>();
        for (User existingUser : existingUsers) {
            if (existingUser.getEmail() != null) {
                usedEmails.add(existingUser.getEmail().toLowerCase(Locale.ROOT));
            }
        }

        int usersToGenerate = Math.max(TARGET_USER_COUNT - existingUsers.size(), 0);
        List<User> generatedUsers = new ArrayList<>();
        for (int i = 0; i < usersToGenerate; i++) {
            String firstName = normalizePersonName(faker.name().firstName(), "User");
            String lastName = normalizePersonName(faker.name().lastName(), "Member");

            generatedUsers.add(User.builder()
                    .email(generateUniqueRealisticEmail(firstName, lastName, random, usedEmails))
                    .password(encodedPassword)
                    .firstName(firstName)
                    .lastName(lastName)
                    .avatarUrl("https://picsum.photos/seed/user" + (i + 1) + "/200/200")
                    .biography(faker.lorem().paragraph())
                    .city(faker.address().city())
                    .nationality("Việt Nam")
                    .isVerified(true)
                    .roles(List.of(userRole))
                    .build());
        }

        generatedUsers = userRepository.saveAll(generatedUsers);

        List<User> users = new ArrayList<>(existingUsers);
        users.addAll(generatedUsers);

        log.info(">>> Seeded {} users ({} existing + {} generated)", users.size(), existingUsers.size(),
                generatedUsers.size());
        return users;
    }

    private List<Workspace> seedWorkspaces(Random random, List<User> users, LocalDateTime now) {
        List<Workspace> workspaces = new ArrayList<>();
        for (int i = 0; i < WORKSPACE_NAMES.length; i++) {
            workspaces.add(Workspace.builder()
                    .name(WORKSPACE_NAMES[i])
                    .owner(users.get(i % users.size()))
                    .createdAt(now.minusDays(random.nextInt(60) + 30))
                    .updatedAt(now.minusDays(random.nextInt(10)))
                    .build());
        }
        workspaces = workspaceRepository.saveAll(workspaces);
        log.info(">>> Seeded {} workspaces", workspaces.size());
        return workspaces;
    }

    private Map<Long, List<User>> seedWorkspaceMembers(Random random, List<Workspace> workspaces, List<User> users) {
        Map<Long, List<User>> wsMembersMap = new HashMap<>();
        List<WorkspaceMember> allMembers = new ArrayList<>();

        // Assign each user to exactly one primary workspace so every user has project
        // visibility.
        Map<Long, List<User>> primaryAssignment = new HashMap<>();
        for (Workspace workspace : workspaces) {
            primaryAssignment.put(workspace.getId(), new ArrayList<>());
        }
        for (int i = 0; i < users.size(); i++) {
            Workspace assignedWorkspace = workspaces.get(i % workspaces.size());
            primaryAssignment.get(assignedWorkspace.getId()).add(users.get(i));
        }

        for (Workspace ws : workspaces) {
            List<User> members = new ArrayList<>();
            members.add(ws.getOwner());
            allMembers.add(WorkspaceMember.builder()
                    .workspace(ws).user(ws.getOwner())
                    .role(WorkspaceMemberRoleType.OWNER)
                    .joinedAt(ws.getCreatedAt())
                    .build());

            Set<String> added = new HashSet<>(Set.of(ws.getOwner().getUserId()));
            List<User> assignedUsers = primaryAssignment.getOrDefault(ws.getId(), List.of());
            for (int j = 0; j < assignedUsers.size(); j++) {
                User candidate = assignedUsers.get(j);
                if (!added.add(candidate.getUserId())) {
                    continue;
                }
                members.add(candidate);
                allMembers.add(WorkspaceMember.builder()
                        .workspace(ws).user(candidate)
                        .role(j == 0 ? WorkspaceMemberRoleType.ADMIN : WorkspaceMemberRoleType.MEMBER)
                        .joinedAt(ws.getCreatedAt().plusDays(random.nextInt(20)))
                        .build());
            }
            wsMembersMap.put(ws.getId(), members);
        }
        workspaceMemberRepository.saveAll(allMembers);
        log.info(">>> Seeded {} workspace members", allMembers.size());
        return wsMembersMap;
    }

    private List<Project> seedProjects(Random random, List<Workspace> workspaces,
            Map<Long, List<User>> wsMembersMap, LocalDateTime now) {
        List<Project> projects = new ArrayList<>();
        int templateIdx = 0;
        for (Workspace ws : workspaces) {
            int projectCount = random.nextInt(4) + 2; // 2–5
            List<User> members = wsMembersMap.get(ws.getId());
            for (int p = 0; p < projectCount; p++) {
                String[] tpl = PROJECT_TEMPLATES[templateIdx++ % PROJECT_TEMPLATES.length];
                ProjectStatusType status = random.nextInt(10) < 7
                        ? ProjectStatusType.ACTIVE
                        : (random.nextBoolean() ? ProjectStatusType.COMPLETED : ProjectStatusType.ARCHIVED);
                projects.add(Project.builder()
                        .workspace(ws)
                        .name(limitLength(tpl[0], 150))
                        .description(limitLength(tpl[1], 2000))
                        .status(status)
                        .createdBy(members.get(random.nextInt(members.size())))
                        .createdAt(ws.getCreatedAt().plusDays(random.nextInt(10)))
                        .updatedAt(now.minusDays(random.nextInt(5)))
                        .build());
            }
        }
        projects = projectRepository.saveAll(projects);
        log.info(">>> Seeded {} projects", projects.size());
        return projects;
    }

    private Map<Long, List<TaskStatus>> seedTaskStatuses(List<Project> projects) {
        List<TaskStatus> all = new ArrayList<>();
        for (Project project : projects) {
            for (int s = 0; s < STATUS_TEMPLATES.length; s++) {
                all.add(TaskStatus.builder()
                        .project(project)
                        .name(STATUS_TEMPLATES[s][0])
                        .code(STATUS_TEMPLATES[s][1])
                        .position(s)
                        .isClosed(Boolean.parseBoolean(STATUS_TEMPLATES[s][2]))
                        .createdAt(project.getCreatedAt())
                        .build());
            }
        }
        all = taskStatusRepository.saveAll(all);
        Map<Long, List<TaskStatus>> map = new HashMap<>();
        for (TaskStatus ts : all) {
            map.computeIfAbsent(ts.getProject().getId(), k -> new ArrayList<>()).add(ts);
        }
        log.info(">>> Seeded {} task statuses", all.size());
        return map;
    }

    private Map<Long, List<TaskType>> seedTaskTypes(List<Project> projects,
            List<Workspace> workspaces, Random random) {
        Map<Long, Workspace> projectWsMap = new HashMap<>();
        for (Project p : projects)
            projectWsMap.put(p.getId(), p.getWorkspace());

        List<TaskType> all = new ArrayList<>();
        for (Project project : projects) {
            int count = random.nextInt(3) + 2; // 2–4
            for (int t = 0; t < count && t < TASK_TYPE_TEMPLATES.length; t++) {
                all.add(TaskType.builder()
                        .workspace(projectWsMap.get(project.getId()))
                        .project(project)
                        .name(TASK_TYPE_TEMPLATES[t][0])
                        .description(TASK_TYPE_TEMPLATES[t][1])
                        .color(TASK_TYPE_TEMPLATES[t][2])
                        .icon(TASK_TYPE_TEMPLATES[t][3])
                        .createdAt(project.getCreatedAt())
                        .updatedAt(project.getCreatedAt())
                        .build());
            }
        }
        all = taskTypeRepository.saveAll(all);
        Map<Long, List<TaskType>> map = new HashMap<>();
        for (TaskType tt : all) {
            map.computeIfAbsent(tt.getProject().getId(), k -> new ArrayList<>()).add(tt);
        }
        log.info(">>> Seeded {} task types", all.size());
        return map;
    }

    private Map<Long, List<Goal>> seedGoals(Random random, List<Project> projects,
            List<Workspace> workspaces, Map<Long, List<User>> wsMembersMap, LocalDateTime now) {
        GoalType[] goalTypes = GoalType.values();
        GoalStatusType[] goalStatuses = GoalStatusType.values();
        List<Goal> all = new ArrayList<>();
        int titleIdx = 0;

        for (Project project : projects) {
            int goalCount = random.nextInt(3) + 3; // 3–5
            List<User> members = wsMembersMap.get(project.getWorkspace().getId());
            for (int g = 0; g < goalCount; g++) {
                GoalStatusType status = goalStatuses[random.nextInt(goalStatuses.length)];
                BigDecimal progress = switch (status) {
                    case COMPLETED -> BigDecimal.valueOf(100);
                    case NOT_STARTED -> BigDecimal.ZERO;
                    default -> BigDecimal.valueOf(random.nextInt(80) + 10)
                            .setScale(2, RoundingMode.HALF_UP);
                };
                all.add(Goal.builder()
                        .project(project)
                        .title(limitLength(GOAL_TITLES[titleIdx++ % GOAL_TITLES.length], 200))
                        .goalType(goalTypes[random.nextInt(goalTypes.length)])
                        .status(status)
                        .progressPercent(progress)
                        .createdBy(members.get(random.nextInt(members.size())))
                        .createdAt(project.getCreatedAt().plusDays(random.nextInt(5)))
                        .updatedAt(now.minusDays(random.nextInt(10)))
                        .build());
            }
        }
        all = goalRepository.saveAll(all);
        Map<Long, List<Goal>> map = new HashMap<>();
        for (Goal g : all) {
            map.computeIfAbsent(g.getProject().getId(), k -> new ArrayList<>()).add(g);
        }
        log.info(">>> Seeded {} goals", all.size());
        return map;
    }

    private List<Task> seedTasks(Faker faker, Random random, List<Project> projects,
            Map<Long, List<Goal>> goalsMap, Map<Long, List<TaskStatus>> statusesMap,
            Map<Long, List<TaskType>> taskTypesMap, List<Workspace> workspaces,
            Map<Long, List<User>> wsMembersMap, LocalDateTime now) {
        TaskPriorityType[] priorities = TaskPriorityType.values();
        ImportanceLevel[] importances = ImportanceLevel.values();
        UrgencyLevel[] urgencies = UrgencyLevel.values();
        SourceViewType[] sources = SourceViewType.values();
        Map<Long, Integer> boardPositions = new HashMap<>(); // per status ID

        List<Task> tasks = new ArrayList<>();
        int titleIdx = 0;

        for (Project project : projects) {
            List<Goal> projectGoals = goalsMap.getOrDefault(project.getId(), List.of());
            List<TaskStatus> statuses = statusesMap.get(project.getId());
            List<TaskType> types = taskTypesMap.getOrDefault(project.getId(), List.of());
            List<User> members = wsMembersMap.get(project.getWorkspace().getId());

            // Tasks with goals
            for (Goal goal : projectGoals) {
                int taskCount = random.nextInt(21) + 10; // 10–30
                for (int t = 0; t < taskCount; t++) {
                    TaskStatus status = statuses.get(random.nextInt(statuses.size()));
                    int boardPos = boardPositions.merge(status.getId(), 1, Integer::sum) - 1;
                    boolean completed = Boolean.TRUE.equals(status.getIsClosed());

                    tasks.add(Task.builder()
                            .project(project).goal(goal).status(status)
                            .title(limitLength(TASK_TITLES[titleIdx++ % TASK_TITLES.length], 200))
                            .description(random.nextInt(10) < 6 ? limitLength(faker.lorem().paragraph(), 5000) : null)
                            .priority(priorities[random.nextInt(priorities.length)])
                            .taskType(types.isEmpty() ? null : types.get(random.nextInt(types.size())))
                            .assignee(random.nextInt(10) < 7 ? members.get(random.nextInt(members.size())) : null)
                            .createdBy(members.get(random.nextInt(members.size())))
                            .dueDate(random.nextInt(10) < 7 ? now.plusDays(random.nextInt(60) - 20) : null)
                            .estimatedMinutes((random.nextInt(8) + 1) * 30)
                            .importanceLevel(importances[random.nextInt(importances.length)])
                            .urgencyLevel(urgencies[random.nextInt(urgencies.length)])
                            .sourceView(sources[random.nextInt(sources.length)])
                            .boardPosition(boardPos)
                            .isCompleted(completed)
                            .completedAt(completed ? now.minusDays(random.nextInt(15)) : null)
                            .createdAt(project.getCreatedAt().plusDays(random.nextInt(20)))
                            .updatedAt(now.minusDays(random.nextInt(7)))
                            .build());
                }
            }

            // Tasks without goals
            int noGoalCount = random.nextInt(6) + 5; // 5–10
            for (int t = 0; t < noGoalCount; t++) {
                TaskStatus status = statuses.get(random.nextInt(statuses.size()));
                int boardPos = boardPositions.merge(status.getId(), 1, Integer::sum) - 1;
                boolean completed = Boolean.TRUE.equals(status.getIsClosed());

                tasks.add(Task.builder()
                        .project(project).goal(null).status(status)
                        .title(limitLength(TASK_TITLES[titleIdx++ % TASK_TITLES.length], 200))
                        .description(random.nextInt(10) < 4 ? limitLength(faker.lorem().paragraph(), 5000) : null)
                        .priority(priorities[random.nextInt(priorities.length)])
                        .taskType(types.isEmpty() ? null : types.get(random.nextInt(types.size())))
                        .assignee(random.nextInt(10) < 5 ? members.get(random.nextInt(members.size())) : null)
                        .createdBy(members.get(random.nextInt(members.size())))
                        .dueDate(random.nextInt(10) < 5 ? now.plusDays(random.nextInt(30)) : null)
                        .estimatedMinutes((random.nextInt(8) + 1) * 30)
                        .importanceLevel(importances[random.nextInt(importances.length)])
                        .urgencyLevel(urgencies[random.nextInt(urgencies.length)])
                        .sourceView(SourceViewType.KANBAN)
                        .boardPosition(boardPos)
                        .isCompleted(completed)
                        .completedAt(completed ? now.minusDays(random.nextInt(10)) : null)
                        .createdAt(project.getCreatedAt().plusDays(random.nextInt(15)))
                        .updatedAt(now.minusDays(random.nextInt(5)))
                        .build());
            }
        }
        tasks = taskRepository.saveAll(tasks);
        log.info(">>> Seeded {} tasks", tasks.size());
        return tasks;
    }

    private void seedTaskSchedules(Random random, List<Task> tasks, List<Project> projects,
            List<Workspace> workspaces, Map<Long, List<User>> wsMembersMap, LocalDateTime now) {
        List<TaskSchedule> schedules = new ArrayList<>();
        for (Task task : tasks) {
            if (random.nextInt(100) >= 30)
                continue; // ~30% get schedules
            List<User> members = wsMembersMap.get(task.getProject().getWorkspace().getId());
            LocalDateTime start = now.plusDays(random.nextInt(30) - 10)
                    .withHour(random.nextInt(10) + 8)
                    .withMinute(random.nextBoolean() ? 0 : 30)
                    .withSecond(0).withNano(0);
            schedules.add(TaskSchedule.builder()
                    .task(task)
                    .scheduledStart(start)
                    .scheduledEnd(start.plusHours(random.nextInt(3) + 1))
                    .scheduledDate(start.toLocalDate())
                    .createdBy(members.get(random.nextInt(members.size())))
                    .createdAt(task.getCreatedAt())
                    .updatedAt(task.getUpdatedAt())
                    .build());
        }
        taskScheduleRepository.saveAll(schedules);
        log.info(">>> Seeded {} task schedules", schedules.size());
    }

    private void seedTaskComments(Random random, List<Task> tasks, List<Project> projects,
            List<Workspace> workspaces, Map<Long, List<User>> wsMembersMap) {
        List<TaskComment> comments = new ArrayList<>();
        for (Task task : tasks) {
            if (random.nextInt(100) >= 25)
                continue; // ~25% get comments
            List<User> members = wsMembersMap.get(task.getProject().getWorkspace().getId());
            int count = random.nextInt(3) + 1;
            for (int c = 0; c < count; c++) {
                comments.add(TaskComment.builder()
                        .task(task)
                        .user(members.get(random.nextInt(members.size())))
                        .content(limitLength(COMMENT_TEMPLATES[random.nextInt(COMMENT_TEMPLATES.length)], 1000))
                        .createdAt(task.getCreatedAt().plusDays(random.nextInt(10) + 1))
                        .updatedAt(task.getCreatedAt().plusDays(random.nextInt(10) + 1))
                        .build());
            }
        }
        taskCommentRepository.saveAll(comments);
        log.info(">>> Seeded {} task comments", comments.size());
    }

    private void seedTaskCheckItems(Random random, List<Task> tasks) {
        List<TaskCheckItem> items = new ArrayList<>();
        for (Task task : tasks) {
            if (random.nextInt(100) >= 20)
                continue; // ~20% get check items
            int count = random.nextInt(4) + 2;
            for (int ci = 0; ci < count; ci++) {
                items.add(TaskCheckItem.builder()
                        .task(task)
                        .title(limitLength(CHECK_ITEM_TITLES[(items.size() + ci) % CHECK_ITEM_TITLES.length], 200))
                        .isChecked(random.nextBoolean())
                        .position(ci)
                        .createdAt(task.getCreatedAt().plusDays(1))
                        .updatedAt(task.getCreatedAt().plusDays(random.nextInt(5) + 1))
                        .build());
            }
        }
        taskCheckItemRepository.saveAll(items);
        log.info(">>> Seeded {} task check items", items.size());
    }

    private void seedWorkspaceTeams(Random random, List<Workspace> workspaces,
            Map<Long, List<User>> wsMembersMap) {
        List<WorkspaceTeam> teams = new ArrayList<>();
        int teamIdx = 0;
        for (Workspace ws : workspaces) {
            int count = random.nextInt(2) + 1; // 1–2 teams
            for (int ti = 0; ti < count; ti++) {
                teams.add(WorkspaceTeam.builder()
                        .workspace(ws)
                        .name(limitLength(TEAM_NAMES[teamIdx++ % TEAM_NAMES.length], 150))
                        .description(limitLength(
                                "Team " + TEAM_NAMES[(teamIdx - 1) % TEAM_NAMES.length] + " của " + ws.getName(),
                                2000))
                        .createdBy(ws.getOwner())
                        .createdAt(ws.getCreatedAt().plusDays(2))
                        .updatedAt(ws.getCreatedAt().plusDays(2))
                        .build());
            }
        }
        teams = workspaceTeamRepository.saveAll(teams);

        List<WorkspaceTeamMember> teamMembers = new ArrayList<>();
        for (WorkspaceTeam team : teams) {
            List<User> members = wsMembersMap.get(team.getWorkspace().getId());
            int memberCount = Math.min(random.nextInt(4) + 2, members.size());
            Set<String> added = new HashSet<>();
            for (int m = 0; m < memberCount; m++) {
                User member = members.get(m % members.size());
                if (!added.add(member.getUserId()))
                    continue;
                teamMembers.add(WorkspaceTeamMember.builder()
                        .team(team)
                        .user(member)
                        .joinedAt(team.getCreatedAt().plusDays(random.nextInt(5)))
                        .build());
            }
        }
        workspaceTeamMemberRepository.saveAll(teamMembers);
        log.info(">>> Seeded {} teams, {} team members", teams.size(), teamMembers.size());
    }

    private String generateUniqueRealisticEmail(String firstName, String lastName, Random random,
            Set<String> usedEmails) {
        String firstToken = normalizeEmailToken(firstName);
        String lastToken = normalizeEmailToken(lastName);

        String localPrefix = firstToken + lastToken;
        if (localPrefix.length() < 4) {
            localPrefix = (localPrefix + "member").substring(0, 6);
        }

        String email;
        do {
            int number = 100 + random.nextInt(9900);
            email = localPrefix + number + "@gmail.com";
        } while (!usedEmails.add(email.toLowerCase(Locale.ROOT)));

        return email;
    }

    private String normalizeEmailToken(String value) {
        if (value == null || value.isBlank()) {
            return "user";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        normalized = NON_ASCII_WORD.matcher(normalized).replaceAll("");

        if (normalized.length() < 2) {
            return "user";
        }

        return normalized.length() > 12 ? normalized.substring(0, 12) : normalized;
    }

    private String normalizePersonName(String value, String fallback) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            normalized = fallback;
        }

        if (normalized.length() < 2) {
            normalized = (normalized + fallback).substring(0, Math.min(2, (normalized + fallback).length()));
        }

        return normalized.length() > 50 ? normalized.substring(0, 50) : normalized;
    }

    private String limitLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
