package com.devloopsx.chronelis.configuration;

import com.devloopsx.chronelis.constant.*;
import com.devloopsx.chronelis.domain.*;
import com.devloopsx.chronelis.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@ConditionalOnProperty(
    prefix = "chronelis.seed",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false)
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
  WorkspaceTeamRepository workspaceTeamRepository;
  WorkspaceTeamMemberRepository workspaceTeamMemberRepository;
  ProjectRepository projectRepository;
  ProjectAccessGrantRepository projectAccessGrantRepository;
  GoalRepository goalRepository;
  TaskStatusRepository taskStatusRepository;
  TaskTypeRepository taskTypeRepository;
  TaskRepository taskRepository;
  TaskScheduleRepository taskScheduleRepository;
  TaskCommentRepository taskCommentRepository;
  PomodoroSessionRepository pomodoroSessionRepository;
  WorkspaceInviteRepository workspaceInviteRepository;
  NotificationRepository notificationRepository;
  ActivityLogRepository activityLogRepository;
  TaskDependencyRepository taskDependencyRepository;

  @NonFinal
  @Value("${chronelis.allowed-init}")
  protected Boolean ALLOWED_INIT;

  @NonFinal
  @Value("${chronelis.account.base-password}")
  protected String BASE_PASSWORD;

  @NonFinal
  @Value("${chronelis.seed.reset-collaboration-data:false}")
  protected Boolean RESET_COLLABORATION_DATA;

  static final long RANDOM_SEED = 2025031701L;
  static final int MIN_USER_COUNT = 220;
  static final int USER_MIN_WORKSPACES = 2;
  static final int USER_MAX_WORKSPACES = 3;
  static final String SEED_EMAIL_DOMAIN = "gmail.com";
  static final Pattern NON_ASCII_TOKEN = Pattern.compile("[^a-z0-9]");
  static final Pattern VN_PHONE_PATTERN =
      Pattern.compile("^(\\+84|0)(3[2-9]|5[689]|7[06-9]|8[1-9]|9[0-9])[0-9]{7}$");

  static final Set<String> RESERVED_INIT_EMAILS =
      Set.of("chronelis.admin@gmail.com", "chronelis.user@gmail.com", "thuanmobile1111@gmail.com");

  static final String[] VN_PHONE_PREFIXES = {
    "032", "033", "034", "035", "036", "037", "038", "039", "056", "058", "059", "070", "076",
    "077", "078", "079", "081", "082", "083", "084", "085", "086", "087", "088", "089", "090",
    "091", "092", "093", "094", "095", "096", "097", "098", "099"
  };

  static final String[] FIRST_NAMES = {
    "An",
    "Bao",
    "Binh",
    "Chi",
    "Dung",
    "Giang",
    "Ha",
    "Hai",
    "Hanh",
    "Hieu",
    "Hoa",
    "Huong",
    "Huy",
    "Khanh",
    "Kiet",
    "Lam",
    "Lan",
    "Linh",
    "Long",
    "Mai",
    "Minh",
    "Nam",
    "Nga",
    "Ngoc",
    "Nhi",
    "Phong",
    "Phuc",
    "Phuong",
    "Quang",
    "Son",
    "Thao",
    "Trang",
    "Tri",
    "Tuan",
    "Van",
    "Vy",
    "Yen",
    "Aiden",
    "Alex",
    "Amelia",
    "Benjamin",
    "Charlotte",
    "Daniel",
    "Elena",
    "Ethan",
    "Gabriel",
    "Hannah",
    "Isabella",
    "Jacob",
    "James",
    "Liam",
    "Lucas",
    "Mia",
    "Natalie",
    "Noah",
    "Olivia",
    "Sofia",
    "Thomas",
    "William",
    "Yuki",
    "Diego",
    "Mateo",
    "Nora",
    "Ava",
    "Leo"
  };

  static final String[] LAST_NAMES = {
    "Nguyen",
    "Tran",
    "Le",
    "Pham",
    "Hoang",
    "Phan",
    "Vu",
    "Dang",
    "Bui",
    "Do",
    "Ngo",
    "Duong",
    "Ly",
    "Truong",
    "Dinh",
    "Huynh",
    "Vo",
    "Ho",
    "Cao",
    "Luu",
    "Smith",
    "Johnson",
    "Davis",
    "Garcia",
    "Brown",
    "Miller",
    "Wilson",
    "Martin",
    "Anderson",
    "Thomas",
    "Lopez",
    "Hernandez",
    "Kim",
    "Lee",
    "Taylor"
  };

  static final String[] CITIES = {
    "Ho Chi Minh",
    "Ha Noi",
    "Da Nang",
    "Can Tho",
    "Nha Trang",
    "Hai Phong",
    "Hue",
    "Singapore",
    "Bangkok",
    "Jakarta",
    "Tokyo",
    "Seoul"
  };

  static final String[] BIO_FOCUS = {
    "product discovery", "delivery planning", "backend services", "frontend experience",
    "quality automation", "data operations", "security hardening", "customer onboarding",
    "incident response", "process optimization", "release management", "team enablement"
  };

  static final String[] WORKSPACE_NAMES = {
    "Chronelis Product Platform",
    "Chronelis Mobile Experience",
    "Chronelis Growth and Lifecycle",
    "Chronelis Customer Success Operations",
    "Orchid Retail Digital Core",
    "Orchid Retail Store Operations",
    "Nimbus Commerce Integrations",
    "Nimbus Merchant Success",
    "Atlas Logistics Planning",
    "Atlas Last Mile Operations",
    "Helios Health Coordination",
    "Helios Care Automation",
    "Vertex Finance Intelligence",
    "Vertex Internal Compliance",
    "Aurora Talent and Culture",
    "Aurora Learning Enablement",
    "Mercury Data Governance",
    "Mercury Analytics Platform",
    "Lumen Design Systems",
    "Lumen Brand Studio",
    "Pioneer Partnerships",
    "Pioneer Revenue Operations",
    "Summit Infrastructure Reliability",
    "Summit Security Operations"
  };

  static final String[] TEAM_NAME_POOL = {
    "Backend Team",
    "Frontend Team",
    "Mobile Team",
    "Product Team",
    "QA Team",
    "Design Team",
    "Growth Team",
    "Operations Team",
    "Data Team",
    "Analytics Team",
    "DevOps Team",
    "Customer Success Team",
    "Compliance Team",
    "Security Team",
    "Content Team",
    "Partnership Team"
  };

  static final String[] PROJECT_NAME_POOL = {
    "Authentication Revamp",
    "Workspace Collaboration System",
    "Internal Analytics Dashboard",
    "Notification Center Refresh",
    "Customer Onboarding Flow",
    "Mobile UI Polish",
    "Goal Tracking Reliability",
    "Task Scheduling Upgrade",
    "Kanban Performance Optimization",
    "Permission Audit Trail",
    "Search and Filter Refinement",
    "Release Quality Automation",
    "Partner Integration Gateway",
    "Self-serve Trial Conversion",
    "Design System Consolidation",
    "Incident Response Hardening",
    "Billing Insights Workspace",
    "Team Assignment Experience",
    "Calendar Sync Modernization",
    "Ops Command Center"
  };

  static final String[] PROJECT_FOCUS_AREAS = {
    "Reduce cycle time and improve release confidence across squads.",
    "Increase user activation through clearer onboarding and guided setup.",
    "Stabilize core APIs and remove recurring production regressions.",
    "Improve collaboration visibility for managers and cross-functional leads.",
    "Strengthen reliability, monitoring, and incident response workflows.",
    "Ship measurable UX improvements for daily operational tasks.",
    "Standardize quality checks and handover documentation between teams.",
    "Improve KPI reporting quality for weekly leadership review."
  };

  static final List<TaskTypeTemplate> TASK_TYPE_TEMPLATES =
      List.of(
          new TaskTypeTemplate("Feature", "Feature delivery item", "#2563EB", "sparkles"),
          new TaskTypeTemplate("Bug", "Defect fix and verification", "#DC2626", "bug"),
          new TaskTypeTemplate("Improvement", "Refactor or optimization", "#059669", "wrench"),
          new TaskTypeTemplate("Documentation", "Knowledge base and runbook", "#D97706", "book"),
          new TaskTypeTemplate("Research", "Discovery and validation", "#7C3AED", "search"),
          new TaskTypeTemplate("Operations", "Operational follow-up work", "#475569", "settings"),
          new TaskTypeTemplate("Design", "UI/UX Mockups and assets", "#EC4899", "palette"),
          new TaskTypeTemplate("Review", "Code and architecture review", "#8B5CF6", "eye"),
          new TaskTypeTemplate("Testing", "QA and automated tests", "#10B981", "check-circle"),
          new TaskTypeTemplate("Deployment", "Release to environments", "#F59E0B", "rocket"));

  static final String[] TASK_ACTIONS = {
    "Implement", "Stabilize", "Review", "Optimize", "Finalize", "Audit",
    "Refine", "Prepare", "Validate", "Align", "Document", "Monitor"
  };

  static final String[] TASK_OBJECTS = {
    "role mapping flow", "workspace member onboarding", "real-time board event handling",
    "task assignment lifecycle", "notification routing logic", "calendar schedule conflict checks",
    "goal progress recalculation", "invite usage validation", "team membership sync",
    "project status reporting", "API error boundaries", "deployment runbook"
  };

  static final String[] TASK_CONTEXTS = {
    "for upcoming release", "before stakeholder demo", "based on QA feedback",
    "to reduce production regressions", "for compliance checkpoint", "for customer pilot rollout"
  };

  static final String[] COMMENT_TEMPLATES = {
    "I validated this path on staging. Please double-check edge cases around permission checks.",
    "Progress is good. Remaining blocker is data mapping from legacy payloads.",
    "Updated according to review notes. Need one more pass from QA.",
    "I pushed a safer fallback for timeout handling. Monitoring after next deploy.",
    "Dependency from another team is delayed. Adjusting expected completion date.",
    "Scope is clear now, I will split this into smaller follow-up tasks.",
    "Could you confirm whether this behavior is expected for owner and admin roles?",
    "I attached screenshots for the empty state and validation branch in the task drawer.",
    "Blocking issue: API returns stale status ordering after rapid drag and drop.",
    "I can take the follow-up test cases for overdue and unscheduled task scenarios.",
    "The latest patch fixes the crash, but we still need better error wording for end users.",
    "Please review the migration note before we enable this in production.",
    "I verified dark mode contrast and there are still two low-contrast hover states.",
    "Can we keep backward compatibility for one release to avoid breaking old clients?",
    "I synced with operations and we can run this rollout in two waves next week.",
    "I left a checklist in notesHtml so QA can retest quickly after merge.",
    "This query is still heavy on larger workspaces; I propose adding an index.",
    "Looks good for MVP, but we need stronger audit metadata for compliance.",
    "I am waiting for partnership payload samples to finish the mapper.",
    "I confirmed the rule with product: owner cannot be removed from workspace."
  };

  static final String[] REPLY_TEMPLATES = {
    "Agreed. I will take this item and update the task notes.",
    "Thanks, I reproduced the issue and attached details in the notes section.",
    "Aligned. We can close this once the smoke test passes.",
    "I coordinated with the owner team and we now have a workaround.",
    "Noted. I will adjust acceptance criteria for this sprint."
  };

  @Override
  public void run(ApplicationArguments args) {
    if (!ALLOWED_INIT) {
      log.info(">>> SKIP SEED DATA - ALLOWED_INIT is false");
      return;
    }

    TransactionTemplate tx = new TransactionTemplate(transactionManager);

    if (Boolean.TRUE.equals(RESET_COLLABORATION_DATA)) {
      log.info(">>> RESET COLLABORATION DATA BEFORE SEEDING");
      tx.executeWithoutResult(status -> resetCollaborationData());
    }

    if (workspaceRepository.count() > 0) {
      log.info(">>> SKIP SEED DATA - workspaces already exist");
      return;
    }

    log.info(">>> START REALISTIC DEMO SEEDING");
    Random random = new Random(RANDOM_SEED);
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

    tx.executeWithoutResult(status -> seedAll(random, now));

    log.info(">>> END REALISTIC DEMO SEEDING");
  }

  private void resetCollaborationData() {
    activityLogRepository.deleteAllInBatch();
    notificationRepository.deleteAllInBatch();
    taskCommentRepository.deleteAllInBatch();
    taskScheduleRepository.deleteAllInBatch();
    pomodoroSessionRepository.deleteAllInBatch();
    taskDependencyRepository.deleteAllInBatch();
    taskRepository.deleteAllInBatch();
    taskTypeRepository.deleteAllInBatch();
    taskStatusRepository.deleteAllInBatch();
    goalRepository.deleteAllInBatch();
    projectAccessGrantRepository.deleteAllInBatch();
    projectRepository.deleteAllInBatch();
    workspaceTeamMemberRepository.deleteAllInBatch();
    workspaceTeamRepository.deleteAllInBatch();
    workspaceInviteRepository.deleteAllInBatch();
    workspaceMemberRepository.deleteAllInBatch();
    workspaceRepository.deleteAllInBatch();
  }

  private void seedAll(Random random, LocalDateTime now) {
    log.info(">>> SEED STEP: users");
    UserSeedResult userSeed = seedUsers(random);

    log.info(">>> SEED STEP: workspaces");
    List<Workspace> workspaces = seedWorkspaces(random, now, userSeed.allUsers());

    log.info(">>> SEED STEP: workspace members");
    MembershipSeedResult membershipSeed =
        seedWorkspaceMembers(random, now, workspaces, userSeed.allUsers());

    log.info(">>> SEED STEP: teams");
    TeamSeedResult teamSeed = seedWorkspaceTeams(random, now, workspaces, membershipSeed);

    log.info(">>> SEED STEP: projects and goals");
    List<Project> projects = seedProjects(random, now, workspaces, membershipSeed, teamSeed);
    Map<Long, List<Project>> projectsByWorkspaceId = groupProjectsByWorkspace(projects);
    seedProjectAccessGrants(random, now, projects, membershipSeed);

    Map<Long, List<TaskStatus>> statusesByProjectId = seedTaskStatuses(projects);
    Map<Long, List<Goal>> goalsByProjectId =
        seedGoals(random, now, projects, membershipSeed, teamSeed);
    Map<Long, List<TaskType>> taskTypesByProjectId =
        seedTaskTypes(random, now, projects, goalsByProjectId);

    log.info(">>> SEED STEP: tasks and schedules");
    TaskSeedResult taskSeed =
        seedTasks(
            random,
            now,
            projects,
            goalsByProjectId,
            statusesByProjectId,
            taskTypesByProjectId,
            membershipSeed,
            teamSeed);

    log.info(">>> SEED STEP: task dependencies");
    List<TaskDependency> taskDependencies = seedTaskDependencies(random, now, taskSeed.tasks());

    ScheduleSeedResult scheduleSeed = seedTaskSchedules(random, now, taskSeed.tasks());

    log.info(">>> SEED STEP: comments");
    CommentSeedResult commentSeed = seedTaskComments(random, now, taskSeed.tasks(), membershipSeed);

    log.info(">>> SEED STEP: pomodoro sessions");
    seedPomodoroSessions(random, now, taskSeed.tasks(), membershipSeed, scheduleSeed);

    log.info(">>> SEED STEP: invites and notifications");
    List<WorkspaceInvite> invites = seedWorkspaceInvites(random, now, workspaces, membershipSeed);
    List<Notification> notifications =
        seedNotifications(
            random,
            now,
            membershipSeed.memberships(),
            taskSeed.tasks(),
            scheduleSeed.schedulesByTaskId(),
            commentSeed.commentsByTaskId(),
            goalsByProjectId,
            invites);

    log.info(">>> SEED STEP: activity logs");
    List<ActivityLog> activityLogs =
        seedActivityLogs(
            random,
            now,
            workspaces,
            membershipSeed,
            teamSeed,
            projectsByWorkspaceId,
            goalsByProjectId,
            taskTypesByProjectId,
            taskSeed.tasks(),
            scheduleSeed.schedules(),
            commentSeed.comments(),
            invites);

    int goalCount = goalsByProjectId.values().stream().mapToInt(List::size).sum();
    int taskTypeCount = taskTypesByProjectId.values().stream().mapToInt(List::size).sum();
    int statusCount = statusesByProjectId.values().stream().mapToInt(List::size).sum();
    double avgWorkspacesPerUser =
        userSeed.allUsers().isEmpty()
            ? 0D
            : (double) membershipSeed.memberships().size() / userSeed.allUsers().size();

    log.info(
        ">>> REALISTIC SEED SUMMARY: users(new)={}, users(total)={}, workspaces={}, workspaceMembers={}, avgWorkspacesPerUser={}, teams={}, teamMembers={}, projects={}, goals={}, taskStatuses={}, taskTypes={}, tasks={}, taskSchedules={}, taskComments={}, invites={}, notifications={}, activityLogs={}",
        userSeed.createdUsers(),
        userSeed.allUsers().size(),
        workspaces.size(),
        membershipSeed.memberships().size(),
        String.format(Locale.ROOT, "%.2f", avgWorkspacesPerUser),
        teamSeed.teams().size(),
        teamSeed.teamMembers().size(),
        projects.size(),
        goalCount,
        statusCount,
        taskTypeCount,
        taskSeed.tasks().size(),
        taskDependencies.size(),
        scheduleSeed.schedules().size(),
        commentSeed.comments().size(),
        invites.size(),
        notifications.size(),
        activityLogs.size());
  }

  private UserSeedResult seedUsers(Random random) {
    Role userRole =
        roleRepository
            .findByName(RoleType.USER_ROLE.getName())
            .orElseThrow(
                () -> new IllegalStateException("USER role not found. Run DataInitializer first."));

    List<User> existingUsers = new ArrayList<>(userRepository.findAll());
    existingUsers.sort(Comparator.comparing(user -> nullableLower(user.getEmail())));

    Set<String> usedEmails = new HashSet<>();
    Set<String> usedPhones = new HashSet<>();
    for (User user : existingUsers) {
      if (user.getEmail() != null) {
        usedEmails.add(user.getEmail().toLowerCase(Locale.ROOT));
      }
      if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
        usedPhones.add(user.getPhoneNumber());
      }
    }

    List<User> seedableUsers = new ArrayList<>();
    List<User> usersToEnrich = new ArrayList<>();
    int profileCursor = 0;
    for (User user : existingUsers) {
      if (!isAllowedSeedEmail(user.getEmail())) {
        continue;
      }

      boolean changed = false;
      String firstName = user.getFirstName();
      String lastName = user.getLastName();

      if (firstName == null || firstName.isBlank() || firstName.trim().length() < 2) {
        firstName = FIRST_NAMES[profileCursor % FIRST_NAMES.length];
        user.setFirstName(firstName);
        changed = true;
      }
      if (lastName == null || lastName.isBlank() || lastName.trim().length() < 2) {
        lastName = LAST_NAMES[(profileCursor * 3 + 1) % LAST_NAMES.length];
        user.setLastName(lastName);
        changed = true;
      }

      if (!isValidSeedPhone(user.getPhoneNumber())) {
        user.setPhoneNumber(generateUniqueVietnamPhone(random, usedPhones));
        changed = true;
      }

      String firstToken = normalizeToken(firstName);
      String lastToken = normalizeToken(lastName);
      String nickname =
          (firstToken + lastToken).length() > 20
              ? (firstToken + lastToken).substring(0, 20)
              : firstToken + lastToken;

      if (user.getNickname() == null
          || user.getNickname().isBlank()
          || user.getNickname().trim().length() < 2) {
        user.setNickname(nickname);
        changed = true;
      }

      if (user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) {
        user.setAvatarUrl(
            "https://api.dicebear.com/7.x/initials/svg?seed=" + firstName + "%20" + lastName);
        changed = true;
      }

      if (user.getBiography() == null || user.getBiography().isBlank()) {
        String focus = BIO_FOCUS[(profileCursor * 5 + 2) % BIO_FOCUS.length];
        user.setBiography(
            "Contributes to "
                + focus
                + " and works closely with cross-functional teams to deliver measurable outcomes.");
        changed = true;
      }

      if (user.getCity() == null || user.getCity().isBlank()) {
        user.setCity(CITIES[(profileCursor * 7 + 1) % CITIES.length]);
        changed = true;
      }

      if (user.getNationality() == null || user.getNationality().isBlank()) {
        user.setNationality("Vietnam");
        changed = true;
      }

      if (!Boolean.TRUE.equals(user.getIsVerified())) {
        user.setIsVerified(true);
        changed = true;
      }

      if (user.getRoles() == null || user.getRoles().isEmpty()) {
        user.setRoles(List.of(userRole));
        changed = true;
      }

      if (changed) {
        usersToEnrich.add(user);
      }

      seedableUsers.add(user);
      profileCursor++;
    }

    if (!usersToEnrich.isEmpty()) {
      userRepository.saveAll(usersToEnrich);
    }

    int usersToCreate = Math.max(MIN_USER_COUNT - seedableUsers.size(), 0);
    if (usersToCreate == 0) {
      seedableUsers.sort(Comparator.comparing(user -> nullableLower(user.getEmail())));
      return new UserSeedResult(seedableUsers, 0);
    }

    String encodedPassword = passwordEncoder.encode(BASE_PASSWORD);
    List<User> generated = new ArrayList<>();
    int pointer = existingUsers.size();
    while (generated.size() < usersToCreate) {
      String firstName = FIRST_NAMES[pointer % FIRST_NAMES.length];
      String lastName = LAST_NAMES[(pointer * 3 + 1) % LAST_NAMES.length];
      String city = CITIES[(pointer * 5 + 2) % CITIES.length];
      String focus = BIO_FOCUS[(pointer * 7 + 3) % BIO_FOCUS.length];
      String email = generateUniqueEmail(firstName, lastName, usedEmails);
      String phoneNumber = generateUniqueVietnamPhone(random, usedPhones);

      String firstToken = normalizeToken(firstName);
      String lastToken = normalizeToken(lastName);
      String nickname =
          (firstToken + lastToken).length() > 20
              ? (firstToken + lastToken).substring(0, 20)
              : firstToken + lastToken;

      generated.add(
          User.builder()
              .email(email)
              .password(encodedPassword)
              .firstName(firstName)
              .lastName(lastName)
              .nickname(nickname)
              .phoneNumber(phoneNumber)
              .avatarUrl(
                  "https://api.dicebear.com/7.x/initials/svg?seed=" + firstName + "%20" + lastName)
              .biography(
                  "Works across "
                      + focus
                      + " and collaborates closely with product and engineering.")
              .city(city)
              .nationality("Vietnam")
              .isVerified(true)
              .roles(List.of(userRole))
              .build());

      pointer++;
    }

    List<User> savedUsers = userRepository.saveAll(generated);
    List<User> allUsers = new ArrayList<>(seedableUsers);
    allUsers.addAll(savedUsers);
    allUsers.sort(Comparator.comparing(user -> nullableLower(user.getEmail())));

    return new UserSeedResult(allUsers, savedUsers.size());
  }

  private List<Workspace> seedWorkspaces(Random random, LocalDateTime now, List<User> users) {
    List<Workspace> workspaces = new ArrayList<>();
    List<User> ownerCandidates = new ArrayList<>(users);
    ownerCandidates.sort(Comparator.comparing(user -> nullableLower(user.getEmail())));
    Collections.shuffle(ownerCandidates, random);

    // Pin thuanmobile1111@gmail.com as owner of the first workspace
    ownerCandidates.stream()
        .filter(u -> "thuanmobile1111@gmail.com".equalsIgnoreCase(u.getEmail()))
        .findFirst()
        .ifPresent(
            pinned -> {
              ownerCandidates.remove(pinned);
              ownerCandidates.add(0, pinned);
            });

    for (int index = 0; index < WORKSPACE_NAMES.length; index++) {
      User owner = ownerCandidates.get(index % ownerCandidates.size());
      LocalDateTime createdAt = now.minusDays(310L - (index * 8L) + random.nextInt(10));
      LocalDateTime updatedAt =
          randomDateTimeBetween(random, createdAt.plusDays(20), now.minusDays(1));

      workspaces.add(
          Workspace.builder()
              .name(WORKSPACE_NAMES[index])
              .owner(owner)
              .createdAt(createdAt)
              .updatedAt(updatedAt)
              .build());
    }

    return workspaceRepository.saveAll(workspaces);
  }

  private MembershipSeedResult seedWorkspaceMembers(
      Random random, LocalDateTime now, List<Workspace> workspaces, List<User> users) {
    List<WorkspaceMember> memberships = new ArrayList<>();
    Map<Long, List<User>> membersByWorkspaceId = new LinkedHashMap<>();
    Map<Long, List<User>> adminsByWorkspaceId = new LinkedHashMap<>();

    List<User> orderedUsers = new ArrayList<>(users);
    orderedUsers.sort(Comparator.comparing(user -> nullableLower(user.getEmail())));

    Map<String, Integer> targetMembershipCountByUserId = new HashMap<>();
    Map<String, Integer> assignedMembershipCountByUserId = new HashMap<>();
    for (User user : orderedUsers) {
      int targetMembership = USER_MIN_WORKSPACES + (random.nextDouble() < 0.48 ? 1 : 0);
      targetMembershipCountByUserId.put(user.getUserId(), targetMembership);
      assignedMembershipCountByUserId.put(user.getUserId(), 0);
    }

    List<LinkedHashSet<User>> workspaceMemberSets = new ArrayList<>();
    List<Integer> workspaceTargets = new ArrayList<>();
    Map<String, Integer> primaryWorkspaceIndexByUserId = new HashMap<>();

    for (int workspaceIndex = 0; workspaceIndex < workspaces.size(); workspaceIndex++) {
      Workspace workspace = workspaces.get(workspaceIndex);
      int targetMembers =
          Math.min(resolveWorkspaceMemberTarget(workspaceIndex, users.size()), users.size());
      workspaceTargets.add(targetMembers);

      LinkedHashSet<User> selected = new LinkedHashSet<>();
      selected.add(workspace.getOwner());
      workspaceMemberSets.add(selected);

      String ownerId = workspace.getOwner().getUserId();
      assignedMembershipCountByUserId.merge(ownerId, 1, Integer::sum);
      targetMembershipCountByUserId.compute(
          ownerId, (key, value) -> Math.max(value == null ? USER_MIN_WORKSPACES : value, 1));
      primaryWorkspaceIndexByUserId.putIfAbsent(ownerId, workspaceIndex);
    }

    for (int userIndex = 0; userIndex < orderedUsers.size(); userIndex++) {
      User user = orderedUsers.get(userIndex);
      String userId = user.getUserId();

      int preferredWorkspaceIndex =
          primaryWorkspaceIndexByUserId.getOrDefault(
              userId,
              Math.floorMod(
                  (userIndex * 9) + random.nextInt(workspaces.size()), workspaces.size()));
      primaryWorkspaceIndexByUserId.putIfAbsent(userId, preferredWorkspaceIndex);

      int targetMembership =
          targetMembershipCountByUserId.getOrDefault(userId, USER_MIN_WORKSPACES);
      int assignedMembership = assignedMembershipCountByUserId.getOrDefault(userId, 0);

      int attempts = 0;
      while (assignedMembership < targetMembership && attempts < workspaces.size() * 4) {
        Integer chosenWorkspaceIndex =
            chooseWorkspaceForUser(
                random, user, preferredWorkspaceIndex, workspaceMemberSets, workspaceTargets);
        if (chosenWorkspaceIndex == null) {
          break;
        }

        if (workspaceMemberSets.get(chosenWorkspaceIndex).add(user)) {
          assignedMembership++;
          assignedMembershipCountByUserId.put(userId, assignedMembership);
        }
        attempts++;
      }

      if (assignedMembership < targetMembership) {
        for (int offset = 0;
            offset < workspaces.size() && assignedMembership < targetMembership;
            offset++) {
          int workspaceIndex = Math.floorMod(preferredWorkspaceIndex + offset, workspaces.size());
          if (workspaceMemberSets.get(workspaceIndex).add(user)) {
            assignedMembership++;
          }
        }
        assignedMembershipCountByUserId.put(userId, assignedMembership);
      }
    }

    users.stream()
        .filter(u -> "thuanmobile1111@gmail.com".equalsIgnoreCase(u.getEmail()))
        .findFirst()
        .ifPresent(
            thuan -> {
              for (LinkedHashSet<User> set : workspaceMemberSets) {
                set.add(thuan);
              }
            });

    for (int workspaceIndex = 0; workspaceIndex < workspaces.size(); workspaceIndex++) {
      Workspace workspace = workspaces.get(workspaceIndex);
      LinkedHashSet<User> selectedMembers = workspaceMemberSets.get(workspaceIndex);

      List<User> orderedMembers = new ArrayList<>(selectedMembers);
      orderedMembers.sort(Comparator.comparing(user -> nullableLower(user.getEmail())));
      orderedMembers.remove(workspace.getOwner());
      Collections.shuffle(orderedMembers, random);
      orderedMembers.add(0, workspace.getOwner());

      List<User> members = new ArrayList<>();
      List<User> admins = new ArrayList<>();
      admins.add(workspace.getOwner());

      List<User> nonOwnerMembers = new ArrayList<>();
      for (User member : orderedMembers) {
        if (!member.getUserId().equals(workspace.getOwner().getUserId())) {
          nonOwnerMembers.add(member);
        }
      }
      nonOwnerMembers.sort(
          Comparator.comparingInt(
                  (User user) ->
                      assignedMembershipCountByUserId.getOrDefault(
                          user.getUserId(), USER_MIN_WORKSPACES))
              .reversed()
              .thenComparing(user -> nullableLower(user.getEmail())));

      int targetAdmins = resolveWorkspaceAdminTarget(orderedMembers.size());
      Set<String> promotedAdminUserIds = new HashSet<>();
      for (int index = 0; index < Math.min(targetAdmins, nonOwnerMembers.size()); index++) {
        promotedAdminUserIds.add(nonOwnerMembers.get(index).getUserId());
      }

      int adminAssigned = 0;
      for (User memberUser : orderedMembers) {
        WorkspaceMemberRoleType role;
        if (memberUser.getUserId().equals(workspace.getOwner().getUserId())) {
          role = WorkspaceMemberRoleType.OWNER;
        } else if (promotedAdminUserIds.contains(memberUser.getUserId())) {
          role = WorkspaceMemberRoleType.MEMBER;
          adminAssigned++;
          admins.add(memberUser);
        } else {
          role = WorkspaceMemberRoleType.MEMBER;
        }

        LocalDateTime joinedAt =
            randomDateTimeBetween(
                random,
                workspace.getCreatedAt().plusHours(2),
                minDateTime(now.minusDays(1), workspace.getCreatedAt().plusDays(120)));

        memberships.add(
            WorkspaceMember.builder()
                .workspace(workspace)
                .user(memberUser)
                .role(role)
                .joinedAt(joinedAt)
                .build());

        members.add(memberUser);
      }

      membersByWorkspaceId.put(workspace.getId(), members);
      adminsByWorkspaceId.put(workspace.getId(), admins);
    }

    List<WorkspaceMember> savedMemberships = workspaceMemberRepository.saveAll(memberships);
    return new MembershipSeedResult(savedMemberships, membersByWorkspaceId, adminsByWorkspaceId);
  }

  private TeamSeedResult seedWorkspaceTeams(
      Random random,
      LocalDateTime now,
      List<Workspace> workspaces,
      MembershipSeedResult membershipSeed) {
    List<WorkspaceTeam> teams = new ArrayList<>();
    for (int workspaceIndex = 0; workspaceIndex < workspaces.size(); workspaceIndex++) {
      Workspace workspace = workspaces.get(workspaceIndex);
      List<User> admins =
          membershipSeed.adminsByWorkspaceId().getOrDefault(workspace.getId(), List.of());
      List<User> workspaceMembers =
          membershipSeed.membersByWorkspaceId().getOrDefault(workspace.getId(), List.of());

      int teamCount = resolveWorkspaceTeamCount(workspaceMembers.size(), random);
      List<String> teamNames = buildTeamNamesForWorkspace(workspaceIndex, teamCount);

      for (int teamIndex = 0; teamIndex < teamNames.size(); teamIndex++) {
        User creator =
            admins.isEmpty()
                ? workspace.getOwner()
                : admins.get((teamIndex + random.nextInt(admins.size())) % admins.size());
        LocalDateTime createdAt =
            randomDateTimeBetween(
                random,
                workspace.getCreatedAt().plusDays(7),
                minDateTime(now.minusDays(2), workspace.getCreatedAt().plusDays(140)));

        teams.add(
            WorkspaceTeam.builder()
                .workspace(workspace)
                .name(teamNames.get(teamIndex))
                .description(
                    limitLength(
                        "Team "
                            + teamNames.get(teamIndex)
                            + " handles cross-functional delivery and weekly planning for "
                            + workspace.getName()
                            + ".",
                        2000))
                .createdBy(creator)
                .createdAt(createdAt)
                .updatedAt(createdAt.plusDays(1 + random.nextInt(8)))
                .build());
      }
    }

    List<WorkspaceTeam> savedTeams = workspaceTeamRepository.saveAll(teams);
    Map<Long, List<WorkspaceTeam>> teamsByWorkspaceId = new LinkedHashMap<>();
    for (WorkspaceTeam team : savedTeams) {
      teamsByWorkspaceId
          .computeIfAbsent(team.getWorkspace().getId(), key -> new ArrayList<>())
          .add(team);
    }

    List<WorkspaceTeamMember> teamMembers = new ArrayList<>();
    Map<Long, List<User>> membersByTeamId = new LinkedHashMap<>();
    for (WorkspaceTeam team : savedTeams) {
      List<User> workspaceMembers =
          membershipSeed
              .membersByWorkspaceId()
              .getOrDefault(team.getWorkspace().getId(), List.of());
      if (workspaceMembers.isEmpty()) {
        continue;
      }

      int maxSize = workspaceMembers.size();
      int minTeamSize = Math.min(4, maxSize);
      int maxTeamSize = Math.min(maxSize, Math.max(minTeamSize, 7 + random.nextInt(7)));
      int teamSize = minTeamSize;
      if (maxTeamSize > minTeamSize) {
        teamSize = minTeamSize + random.nextInt(maxTeamSize - minTeamSize + 1);
      }

      LinkedHashSet<User> selected = new LinkedHashSet<>();
      selected.add(team.getCreatedBy());
      while (selected.size() < teamSize) {
        selected.add(workspaceMembers.get(random.nextInt(workspaceMembers.size())));
      }

      List<User> selectedUsers = new ArrayList<>(selected);
      membersByTeamId.put(team.getId(), selectedUsers);

      for (User user : selectedUsers) {
        LocalDateTime joinedAt =
            randomDateTimeBetween(
                random,
                team.getCreatedAt(),
                minDateTime(now.minusDays(1), team.getCreatedAt().plusDays(30)));

        teamMembers.add(
            WorkspaceTeamMember.builder().team(team).user(user).joinedAt(joinedAt).build());
      }
    }

    List<WorkspaceTeamMember> savedTeamMembers = workspaceTeamMemberRepository.saveAll(teamMembers);
    return new TeamSeedResult(savedTeams, savedTeamMembers, teamsByWorkspaceId, membersByTeamId);
  }

  private List<Project> seedProjects(
      Random random,
      LocalDateTime now,
      List<Workspace> workspaces,
      MembershipSeedResult membershipSeed,
      TeamSeedResult teamSeed) {
    List<Project> projects = new ArrayList<>();

    for (int workspaceIndex = 0; workspaceIndex < workspaces.size(); workspaceIndex++) {
      Workspace workspace = workspaces.get(workspaceIndex);
      List<User> admins =
          membershipSeed.adminsByWorkspaceId().getOrDefault(workspace.getId(), List.of());
      List<WorkspaceTeam> teams =
          teamSeed.teamsByWorkspaceId().getOrDefault(workspace.getId(), List.of());
      int projectCount = 3 + random.nextInt(2);

      for (int projectIndex = 0; projectIndex < projectCount; projectIndex++) {
        ProjectStatusType status = resolveProjectStatus(random, projectIndex, projectCount);

        WorkspaceTeam managerTeam = null;
        if (!teams.isEmpty() && random.nextDouble() < 0.82) {
          managerTeam = teams.get(random.nextInt(teams.size()));
        }

        User managerUser =
            pickManagerUser(random, managerTeam, workspace, membershipSeed, teamSeed);
        User creator =
            admins.isEmpty() ? workspace.getOwner() : admins.get(random.nextInt(admins.size()));

        LocalDateTime createdAt =
            randomDateTimeBetween(
                random,
                workspace.getCreatedAt().plusDays(8 + projectIndex * 5L),
                minDateTime(now.minusDays(2), workspace.getCreatedAt().plusDays(260)));
        LocalDateTime updatedAt = resolveProjectUpdatedAt(random, now, createdAt, status);

        String name = buildProjectName(workspace, workspaceIndex, projectIndex);
        String description = buildProjectDescription(random, workspace, name);
        ProjectVisibilityType visibility =
            random.nextDouble() < 0.20
                ? ProjectVisibilityType.PRIVATE
                : ProjectVisibilityType.PUBLIC;

        projects.add(
            Project.builder()
                .workspace(workspace)
                .name(limitLength(name, 150))
                .description(limitLength(description, 2000))
                .status(status)
                .visibility(visibility)
                .createdBy(creator)
                .managerUser(managerUser)
                .managerTeam(managerTeam)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build());
      }
    }

    return projectRepository.saveAll(projects);
  }

  private Map<Long, List<Project>> groupProjectsByWorkspace(List<Project> projects) {
    Map<Long, List<Project>> grouped = new LinkedHashMap<>();
    for (Project project : projects) {
      grouped
          .computeIfAbsent(project.getWorkspace().getId(), key -> new ArrayList<>())
          .add(project);
    }
    return grouped;
  }

  private void seedProjectAccessGrants(
      Random random,
      LocalDateTime now,
      List<Project> projects,
      MembershipSeedResult membershipSeed) {
    List<ProjectAccessGrant> grants = new ArrayList<>();

    for (Project project : projects) {
      User workspaceOwner = project.getWorkspace().getOwner();
      List<User> workspaceMembers =
          membershipSeed
              .membersByWorkspaceId()
              .getOrDefault(project.getWorkspace().getId(), List.of());
      Set<String> grantedUserIds = new HashSet<>();
      Set<Long> grantedTeamIds = new HashSet<>();

      if (project.getVisibility() == ProjectVisibilityType.PRIVATE) {
        // Grant MANAGER to managerUser (must not be workspace owner)
        User managerUser = project.getManagerUser();
        if (managerUser != null
            && !managerUser.getUserId().equals(workspaceOwner.getUserId())
            && !grantedUserIds.contains(managerUser.getUserId())) {
          grants.add(
              buildAccessGrant(
                  project,
                  ProjectAccessSubjectType.USER,
                  managerUser,
                  null,
                  ProjectAccessRoleType.MANAGER,
                  workspaceOwner,
                  now,
                  random));
          grantedUserIds.add(managerUser.getUserId());
        }

        // Grant CONTRIBUTOR or VIEWER to 2–4 additional workspace members
        List<User> candidates = new ArrayList<>(workspaceMembers);
        Collections.shuffle(candidates, random);
        int grantCount = Math.min(2 + random.nextInt(3), candidates.size());
        int granted = 0;
        for (User member : candidates) {
          if (granted >= grantCount) {
            break;
          }
          if (member.getUserId().equals(workspaceOwner.getUserId())) {
            continue;
          }
          if (grantedUserIds.contains(member.getUserId())) {
            continue;
          }
          ProjectAccessRoleType role =
              random.nextDouble() < 0.5
                  ? ProjectAccessRoleType.CONTRIBUTOR
                  : ProjectAccessRoleType.VIEWER;
          grants.add(
              buildAccessGrant(
                  project,
                  ProjectAccessSubjectType.USER,
                  member,
                  null,
                  role,
                  workspaceOwner,
                  now,
                  random));
          grantedUserIds.add(member.getUserId());
          granted++;
        }

        // Team grant: CONTRIBUTOR for managerTeam (if present)
        WorkspaceTeam managerTeam = project.getManagerTeam();
        if (managerTeam != null
            && !grantedTeamIds.contains(managerTeam.getId())
            && random.nextDouble() < 0.75) {
          grants.add(
              buildAccessGrant(
                  project,
                  ProjectAccessSubjectType.TEAM,
                  null,
                  managerTeam,
                  ProjectAccessRoleType.CONTRIBUTOR,
                  workspaceOwner,
                  now,
                  random));
          grantedTeamIds.add(managerTeam.getId());
        }
      } else {
        // PUBLIC: optionally elevate managerUser from CONTRIBUTOR to MANAGER
        User managerUser = project.getManagerUser();
        if (managerUser != null
            && !managerUser.getUserId().equals(workspaceOwner.getUserId())
            && !grantedUserIds.contains(managerUser.getUserId())
            && random.nextDouble() < 0.70) {
          grants.add(
              buildAccessGrant(
                  project,
                  ProjectAccessSubjectType.USER,
                  managerUser,
                  null,
                  ProjectAccessRoleType.MANAGER,
                  workspaceOwner,
                  now,
                  random));
          grantedUserIds.add(managerUser.getUserId());
        }
      }
    }

    projectAccessGrantRepository.saveAll(grants);
  }

  private ProjectAccessGrant buildAccessGrant(
      Project project,
      ProjectAccessSubjectType subjectType,
      User user,
      WorkspaceTeam team,
      ProjectAccessRoleType role,
      User grantedBy,
      LocalDateTime now,
      Random random) {
    LocalDateTime createdAt =
        randomDateTimeBetween(
            random,
            project.getCreatedAt().plusDays(1),
            minDateTime(now.minusDays(1), project.getUpdatedAt().plusDays(5)));
    LocalDateTime updatedAt =
        randomDateTimeBetween(random, createdAt, minDateTime(now, createdAt.plusDays(14)));
    return ProjectAccessGrant.builder()
        .project(project)
        .subjectType(subjectType)
        .user(user)
        .team(team)
        .role(role)
        .grantedBy(grantedBy)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .build();
  }

  private int resolveWorkspaceMemberTarget(int workspaceIndex, int userCount) {
    int target;
    if (workspaceIndex < 8) {
      target = 12 + (workspaceIndex % 5);
    } else if (workspaceIndex < 18) {
      target = 18 + (workspaceIndex % 7);
    } else {
      target = 30 + (workspaceIndex % 9);
    }
    return Math.min(target, userCount);
  }

  private int resolveWorkspaceAdminTarget(int memberCount) {
    return Math.max(1, Math.min(6, memberCount / 7));
  }

  private Integer chooseWorkspaceForUser(
      Random random,
      User user,
      int preferredWorkspaceIndex,
      List<LinkedHashSet<User>> workspaceMemberSets,
      List<Integer> workspaceTargets) {
    List<Integer> candidates = new ArrayList<>();
    for (int workspaceIndex = 0; workspaceIndex < workspaceMemberSets.size(); workspaceIndex++) {
      LinkedHashSet<User> members = workspaceMemberSets.get(workspaceIndex);
      int target = workspaceTargets.get(workspaceIndex);
      if (members.contains(user)) {
        continue;
      }
      if (members.size() < target) {
        candidates.add(workspaceIndex);
      }
    }

    if (candidates.isEmpty()) {
      for (int workspaceIndex = 0; workspaceIndex < workspaceMemberSets.size(); workspaceIndex++) {
        if (!workspaceMemberSets.get(workspaceIndex).contains(user)) {
          candidates.add(workspaceIndex);
        }
      }
    }

    if (candidates.isEmpty()) {
      return null;
    }

    int workspaceCount = workspaceMemberSets.size();
    candidates.sort(
        Comparator.comparingDouble(
            workspaceIndex ->
                workspaceSelectionScore(
                    workspaceMemberSets.get(workspaceIndex).size(),
                    workspaceTargets.get(workspaceIndex),
                    circularDistance(workspaceIndex, preferredWorkspaceIndex, workspaceCount))));

    int candidatePool = Math.min(5, candidates.size());
    return candidates.get(random.nextInt(candidatePool));
  }

  private double workspaceSelectionScore(int currentSize, int targetSize, int distance) {
    double fillRatio = targetSize <= 0 ? 1D : (double) currentSize / targetSize;
    return fillRatio + (distance * 0.06D);
  }

  private int circularDistance(int left, int right, int modulo) {
    int direct = Math.abs(left - right);
    int wrap = modulo - direct;
    return Math.min(direct, wrap);
  }

  private int resolveWorkspaceTeamCount(int memberCount, Random random) {
    int base = memberCount >= 30 ? 6 : (memberCount >= 20 ? 5 : 4);
    if (memberCount >= 35 && random.nextDouble() < 0.55) {
      base++;
    }
    return Math.min(base, TEAM_NAME_POOL.length);
  }

  private List<String> buildTeamNamesForWorkspace(int workspaceIndex, int teamCount) {
    List<String> names = new ArrayList<>();
    for (int index = 0; index < teamCount; index++) {
      String baseName = TEAM_NAME_POOL[(workspaceIndex * 3 + index) % TEAM_NAME_POOL.length];
      String candidate = baseName;
      int suffix = 2;
      while (names.contains(candidate)) {
        candidate = baseName + " " + suffix;
        suffix++;
      }
      names.add(candidate);
    }
    return names;
  }

  private ProjectStatusType resolveProjectStatus(
      Random random, int projectIndex, int projectCount) {
    if (projectIndex == 0) {
      return ProjectStatusType.ACTIVE;
    }
    if (projectIndex == 1) {
      return ProjectStatusType.COMPLETED;
    }
    if (projectIndex == projectCount - 1 && random.nextDouble() < 0.25) {
      return ProjectStatusType.ARCHIVED;
    }

    int roll = random.nextInt(100);
    if (roll < 64) {
      return ProjectStatusType.ACTIVE;
    }
    if (roll < 88) {
      return ProjectStatusType.COMPLETED;
    }
    return ProjectStatusType.ARCHIVED;
  }

  private String buildProjectName(Workspace workspace, int workspaceIndex, int projectIndex) {
    String baseName =
        PROJECT_NAME_POOL[(workspaceIndex * 5 + projectIndex) % PROJECT_NAME_POOL.length];
    return baseName + " - " + workspaceProjectTag(workspace.getName());
  }

  private String buildProjectDescription(Random random, Workspace workspace, String projectName) {
    String focusArea = PROJECT_FOCUS_AREAS[random.nextInt(PROJECT_FOCUS_AREAS.length)];
    return projectName + " for " + workspace.getName() + ". " + focusArea;
  }

  private String workspaceProjectTag(String workspaceName) {
    if (workspaceName == null || workspaceName.isBlank()) {
      return "Workspace";
    }

    String[] parts = workspaceName.trim().split("\\s+");
    if (parts.length == 1) {
      return parts[0];
    }
    return parts[0] + " " + parts[1];
  }

  private Map<Long, List<TaskStatus>> seedTaskStatuses(List<Project> projects) {
    List<TaskStatus> statuses = new ArrayList<>();
    for (Project project : projects) {
      statuses.add(
          TaskStatus.builder()
              .project(project)
              .name("Backlog")
              .code("BACKLOG")
              .position(0)
              .isClosed(false)
              .createdAt(project.getCreatedAt().plusHours(2))
              .build());
      statuses.add(
          TaskStatus.builder()
              .project(project)
              .name("To Do")
              .code("TODO")
              .position(1)
              .isClosed(false)
              .createdAt(project.getCreatedAt().plusHours(2))
              .build());
      statuses.add(
          TaskStatus.builder()
              .project(project)
              .name("In Progress")
              .code("IN_PROGRESS")
              .position(2)
              .isClosed(false)
              .createdAt(project.getCreatedAt().plusHours(2))
              .build());
      statuses.add(
          TaskStatus.builder()
              .project(project)
              .name("Review")
              .code("REVIEW")
              .position(3)
              .isClosed(false)
              .createdAt(project.getCreatedAt().plusHours(2))
              .build());
      statuses.add(
          TaskStatus.builder()
              .project(project)
              .name("Done")
              .code("DONE")
              .position(4)
              .isClosed(true)
              .createdAt(project.getCreatedAt().plusHours(2))
              .build());
    }

    List<TaskStatus> savedStatuses = taskStatusRepository.saveAll(statuses);
    Map<Long, List<TaskStatus>> grouped = new LinkedHashMap<>();
    for (TaskStatus status : savedStatuses) {
      grouped.computeIfAbsent(status.getProject().getId(), key -> new ArrayList<>()).add(status);
    }
    for (List<TaskStatus> value : grouped.values()) {
      value.sort(Comparator.comparingInt(TaskStatus::getPosition));
    }
    return grouped;
  }

  private Map<Long, List<Goal>> seedGoals(
      Random random,
      LocalDateTime now,
      List<Project> projects,
      MembershipSeedResult membershipSeed,
      TeamSeedResult teamSeed) {
    List<Goal> goals = new ArrayList<>();

    for (Project project : projects) {
      List<User> workspaceMembers =
          membershipSeed
              .membersByWorkspaceId()
              .getOrDefault(project.getWorkspace().getId(), List.of());
      List<WorkspaceTeam> workspaceTeams =
          teamSeed.teamsByWorkspaceId().getOrDefault(project.getWorkspace().getId(), List.of());

      int goalCount = resolveGoalCount(project.getStatus(), random);
      List<String> goalTitles = buildGoalTitles(project);

      for (int index = 0; index < goalCount; index++) {
        GoalStatusType status = chooseGoalStatus(project.getStatus(), random);
        WorkspaceTeam managerTeam = chooseGoalManagerTeam(random, project, workspaceTeams);
        User managerUser =
            chooseGoalManagerUser(
                random, project, managerTeam, workspaceMembers, membershipSeed, teamSeed);
        User creator =
            workspaceMembers.isEmpty()
                ? project.getCreatedBy()
                : workspaceMembers.get(random.nextInt(workspaceMembers.size()));

        LocalDateTime createdAt =
            randomDateTimeBetween(
                random,
                project.getCreatedAt().plusDays(2),
                minDateTime(now.minusDays(3), project.getUpdatedAt()));
        LocalDateTime updatedAt =
            randomDateTimeBetween(
                random,
                createdAt,
                minDateTime(now.minusDays(1), project.getUpdatedAt().plusDays(20)));

        goals.add(
            Goal.builder()
                .project(project)
                .title(limitLength(goalTitles.get(index % goalTitles.size()), 200))
                .goalType(GoalType.values()[index % GoalType.values().length])
                .status(status)
                .progressPercent(progressForGoalStatus(status, random))
                .createdBy(creator)
                .managerUser(managerUser)
                .managerTeam(managerTeam)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build());
      }
    }

    List<Goal> savedGoals = goalRepository.saveAll(goals);
    Map<Long, List<Goal>> grouped = new LinkedHashMap<>();
    for (Goal goal : savedGoals) {
      grouped.computeIfAbsent(goal.getProject().getId(), key -> new ArrayList<>()).add(goal);
    }
    return grouped;
  }

  private Map<Long, List<TaskType>> seedTaskTypes(
      Random random,
      LocalDateTime now,
      List<Project> projects,
      Map<Long, List<Goal>> goalsByProjectId) {
    List<TaskType> taskTypes = new ArrayList<>();

    for (Project project : projects) {
      List<Goal> goals = goalsByProjectId.getOrDefault(project.getId(), List.of());
      for (TaskTypeTemplate template : TASK_TYPE_TEMPLATES) {
        if ("Operations".equals(template.name())
            && project.getStatus() == ProjectStatusType.ARCHIVED) {
          continue;
        }
        if ("Operations".equals(template.name())
            && project.getStatus() == ProjectStatusType.COMPLETED
            && random.nextDouble() < 0.45) {
          continue;
        }

        Goal linkedGoal =
            goals.isEmpty() || random.nextDouble() >= 0.18
                ? null
                : goals.get(random.nextInt(goals.size()));

        LocalDateTime createdAt =
            randomDateTimeBetween(
                random,
                project.getCreatedAt().plusDays(1),
                minDateTime(now.minusDays(5), project.getCreatedAt().plusDays(20)));
        LocalDateTime updatedAt =
            randomDateTimeBetween(
                random, createdAt, minDateTime(now.minusDays(1), createdAt.plusDays(25)));

        taskTypes.add(
            TaskType.builder()
                .workspace(project.getWorkspace())
                .project(project)
                .goal(linkedGoal)
                .name(template.name())
                .description(template.description())
                .color(template.color())
                .icon(template.icon())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build());
      }
    }

    List<TaskType> savedTaskTypes = taskTypeRepository.saveAll(taskTypes);
    Map<Long, List<TaskType>> grouped = new LinkedHashMap<>();
    for (TaskType taskType : savedTaskTypes) {
      grouped
          .computeIfAbsent(taskType.getProject().getId(), key -> new ArrayList<>())
          .add(taskType);
    }
    return grouped;
  }

  private TaskSeedResult seedTasks(
      Random random,
      LocalDateTime now,
      List<Project> projects,
      Map<Long, List<Goal>> goalsByProjectId,
      Map<Long, List<TaskStatus>> statusesByProjectId,
      Map<Long, List<TaskType>> taskTypesByProjectId,
      MembershipSeedResult membershipSeed,
      TeamSeedResult teamSeed) {
    List<Task> tasks = new ArrayList<>();
    Map<Long, Integer> boardPositionByStatusId = new HashMap<>();

    for (Project project : projects) {
      List<TaskStatus> statuses = statusesByProjectId.getOrDefault(project.getId(), List.of());
      if (statuses.size() < 5) {
        continue;
      }

      List<Goal> goals = goalsByProjectId.getOrDefault(project.getId(), List.of());
      List<TaskType> taskTypes = taskTypesByProjectId.getOrDefault(project.getId(), List.of());

      List<User> workspaceMembers =
          membershipSeed
              .membersByWorkspaceId()
              .getOrDefault(project.getWorkspace().getId(), List.of());
      List<User> members;

      if (project.getVisibility() == ProjectVisibilityType.PRIVATE) {
        Set<User> allowedUsers = new HashSet<>();
        allowedUsers.add(project.getWorkspace().getOwner());
        if (project.getManagerUser() != null) {
          allowedUsers.add(project.getManagerUser());
        }

        List<ProjectAccessGrant> grants =
            projectAccessGrantRepository.findAll().stream()
                .filter(g -> g.getProject().getId().equals(project.getId()))
                .toList();

        for (ProjectAccessGrant grant : grants) {
          if (grant.getUser() != null) {
            allowedUsers.add(grant.getUser());
          }
          if (grant.getTeam() != null) {
            List<User> teamMembers =
                teamSeed.membersByTeamId().getOrDefault(grant.getTeam().getId(), List.of());
            allowedUsers.addAll(teamMembers);
          }
        }
        members = new ArrayList<>(allowedUsers);
      } else {
        members = new ArrayList<>(workspaceMembers);
      }

      if (members.isEmpty()) {
        members = List.of(project.getCreatedBy());
      }

      List<User> collaborationOrder = new ArrayList<>(members);
      collaborationOrder.sort(Comparator.comparing(user -> nullableLower(user.getEmail())));

      List<Task> projectTasks = new ArrayList<>();
      for (Goal goal : goals) {
        int goalTaskCount = 5 + random.nextInt(3);
        for (int index = 0; index < goalTaskCount; index++) {
          projectTasks.add(
              buildTaskEntity(
                  random,
                  now,
                  project,
                  goal,
                  statuses,
                  taskTypes,
                  collaborationOrder,
                  boardPositionByStatusId));
        }
      }

      int noGoalTaskCount = 1 + random.nextInt(2);
      for (int index = 0; index < noGoalTaskCount; index++) {
        projectTasks.add(
            buildTaskEntity(
                random,
                now,
                project,
                null,
                statuses,
                taskTypes,
                collaborationOrder,
                boardPositionByStatusId));
      }

      // Distribute assignees evenly for this project
      int assigneeCursor = 0;
      for (Task task : projectTasks) {
        User assignee = collaborationOrder.get(assigneeCursor % collaborationOrder.size());
        task.setAssignee(assignee);
        assigneeCursor++;
      }
      tasks.addAll(projectTasks);
    }

    List<User> allAssignedUsers =
        membershipSeed.membersByWorkspaceId().values().stream()
            .flatMap(List::stream)
            .distinct()
            .toList();
    User thuan =
        allAssignedUsers.stream()
            .filter(u -> "thuanmobile1111@gmail.com".equalsIgnoreCase(u.getEmail()))
            .findFirst()
            .orElse(null);

    if (thuan != null) {
      for (Task task : tasks) {
        if (random.nextDouble() < 0.3) {
          task.setAssignee(thuan);
        }
      }
    }

    List<Task> savedTasks = taskRepository.saveAll(tasks);
    Map<Long, List<Task>> tasksByProjectId = new LinkedHashMap<>();
    for (Task task : savedTasks) {
      tasksByProjectId
          .computeIfAbsent(task.getProject().getId(), key -> new ArrayList<>())
          .add(task);
    }

    return new TaskSeedResult(savedTasks, tasksByProjectId);
  }

  private Task buildTaskEntity(
      Random random,
      LocalDateTime now,
      Project project,
      Goal goal,
      List<TaskStatus> statuses,
      List<TaskType> taskTypes,
      List<User> collaborationOrder,
      Map<Long, Integer> boardPositionByStatusId) {
    TaskStatus status = chooseTaskStatus(random, project.getStatus(), statuses);
    boolean completed = Boolean.TRUE.equals(status.getIsClosed());

    int creatorIndex = random.nextInt(collaborationOrder.size());
    User creator = collaborationOrder.get(creatorIndex);
    List<User> collaborationCandidates =
        buildCollaborationCandidates(collaborationOrder, creatorIndex, 7);

    User assignee = null;
    if (!collaborationCandidates.isEmpty() && random.nextDouble() < 0.88) {
      assignee = collaborationCandidates.get(random.nextInt(collaborationCandidates.size()));
    } else if (random.nextDouble() < 0.12) {
      assignee = creator;
    }

    TaskType taskType = chooseTaskTypeForTask(random, taskTypes, goal);
    TaskPriorityType priority = choosePriority(random);
    ImportanceLevel importance =
        priority == TaskPriorityType.LOW && random.nextDouble() < 0.7
            ? ImportanceLevel.LOW
            : ImportanceLevel.HIGH;
    UrgencyLevel urgency =
        (priority == TaskPriorityType.URGENT || priority == TaskPriorityType.HIGH)
            ? UrgencyLevel.HIGH
            : (random.nextDouble() < 0.25 ? UrgencyLevel.HIGH : UrgencyLevel.LOW);

    LocalDateTime createdAtEarliest = project.getCreatedAt().plusDays(1);
    LocalDateTime createdAtLatest =
        minDateTime(now.minusHours(6), project.getUpdatedAt().plusDays(30));
    LocalDateTime createdAt;
    if (project.getStatus() == ProjectStatusType.ACTIVE && random.nextDouble() < 0.45) {
      LocalDateTime recentFrom = maxDateTime(createdAtEarliest, now.minusDays(30));
      createdAt =
          recentFrom.isBefore(createdAtLatest)
              ? randomDateTimeBetween(random, recentFrom, createdAtLatest)
              : randomDateTimeBetween(random, createdAtEarliest, createdAtLatest);
    } else {
      createdAt = randomDateTimeBetween(random, createdAtEarliest, createdAtLatest);
    }
    LocalDateTime dueDate =
        resolveTaskDueDate(random, now, createdAt, completed, project.getStatus());
    LocalDateTime completedAt =
        completed ? resolveTaskCompletedAt(random, now, createdAt, dueDate) : null;

    LocalDateTime updatedAt = resolveTaskUpdatedAt(random, now, createdAt, completedAt);
    SourceViewType sourceView = chooseSourceView(random);

    int boardPosition = boardPositionByStatusId.merge(status.getId(), 1, Integer::sum) - 1;

    String title = generateTaskTitle(random, project, goal);
    String description =
        random.nextDouble() < 0.82 ? buildTaskDescription(title, project, goal) : null;
    String notesHtml = random.nextDouble() < 0.35 ? buildTaskNotesHtml(project, random) : null;

    return Task.builder()
        .project(project)
        .goal(goal)
        .status(status)
        .title(limitLength(title, 200))
        .description(description)
        .notesHtml(notesHtml)
        .priority(priority)
        .taskType(taskType)
        .assignee(assignee)
        .createdBy(creator)
        .dueDate(dueDate)
        .estimatedMinutes(estimateMinutes(priority, random))
        .importanceLevel(importance)
        .urgencyLevel(urgency)
        .sourceView(sourceView)
        .boardPosition(boardPosition)
        .isCompleted(completed)
        .completedAt(completedAt)
        .createdAt(createdAt)
        .updatedAt(updatedAt)
        .build();
  }

  private ScheduleSeedResult seedTaskSchedules(Random random, LocalDateTime now, List<Task> tasks) {
    List<TaskSchedule> schedules = new ArrayList<>();

    LocalDateTime nowAnchor = ceilToQuarterHour(now.plusMinutes(10));
    LocalDate anchorDate = nowAnchor.toLocalDate();
    if (nowAnchor.toLocalTime().isAfter(LocalTime.of(17, 30))) {
      anchorDate = anchorDate.plusDays(1);
    }

    int dayWindow = 8;

    List<Task> schedulableTasks = new ArrayList<>();
    for (Task task : tasks) {
      if (!Boolean.TRUE.equals(task.getIsCompleted())) {
        schedulableTasks.add(task);
      }
    }

    if (schedulableTasks.isEmpty()) {
      return new ScheduleSeedResult(List.of(), new LinkedHashMap<>());
    }

    schedulableTasks.sort(
        Comparator.comparing((Task task) -> task.getProject().getId())
            .thenComparing(Task::getCreatedAt)
            .thenComparing(task -> task.getId() == null ? Long.MAX_VALUE : task.getId()));

    int taskCursor = random.nextInt(schedulableTasks.size());

    for (int dayOffset = 0; dayOffset < dayWindow; dayOffset++) {
      LocalDate day = anchorDate.plusDays(dayOffset);
      int targetSchedulesPerDay = 3 + random.nextInt(3); // 3-5 schedules/day

      LocalDateTime dayStart = day.atTime(8, 30);
      if (dayOffset == 0) {
        dayStart = maxDateTime(dayStart, nowAnchor);
      }

      LocalDateTime dayEnd = day.atTime(18, 30);
      if (dayStart == null || dayEnd == null || !dayStart.isBefore(dayEnd.minusMinutes(30))) {
        continue;
      }

      LocalDateTime cursor = ceilToQuarterHour(dayStart);

      for (int scheduleIndex = 0; scheduleIndex < targetSchedulesPerDay; scheduleIndex++) {
        int remainingSlots = targetSchedulesPerDay - scheduleIndex;
        long remainingMinutes = ChronoUnit.MINUTES.between(cursor, dayEnd);
        long minMinutesNeeded = (remainingSlots * 30L) + ((remainingSlots - 1) * 15L);
        if (remainingMinutes < minMinutesNeeded) {
          break;
        }

        Task task = schedulableTasks.get(taskCursor);
        taskCursor = (taskCursor + 1) % schedulableTasks.size();

        long maxDurationForCurrent = remainingMinutes - ((remainingSlots - 1) * (30L + 15L));
        long durationMinutes = resolveScheduleDurationMinutes(random, task.getPriority());
        int durationRetry = 0;
        while (durationMinutes > maxDurationForCurrent && durationRetry < 8) {
          durationMinutes = resolveScheduleDurationMinutes(random, task.getPriority());
          durationRetry++;
        }
        if (durationMinutes > maxDurationForCurrent) {
          long snapped = Math.max(30L, maxDurationForCurrent - (maxDurationForCurrent % 15));
          if (snapped < 30L) {
            break;
          }
          durationMinutes = snapped;
        }

        LocalDateTime start = ceilToQuarterHour(cursor);
        if (start.isBefore(dayStart)) {
          start = ceilToQuarterHour(dayStart);
        }

        LocalDateTime end = start.plusMinutes(durationMinutes);
        if (end.isAfter(dayEnd)) {
          break;
        }

        User creator = task.getAssignee() != null ? task.getAssignee() : task.getCreatedBy();

        LocalDateTime createdLowerBound =
            maxDateTime(task.getCreatedAt().plusMinutes(10), now.minusDays(21));
        LocalDateTime createdUpperBound = minDateTime(now.minusMinutes(1), start.minusMinutes(5));

        LocalDateTime createdAt;
        if (createdUpperBound != null
            && createdLowerBound != null
            && createdUpperBound.isAfter(createdLowerBound)) {
          createdAt = randomDateTimeBetween(random, createdLowerBound, createdUpperBound);
        } else {
          createdAt =
              minDateTime(now.minusMinutes(1), maxDateTime(task.getCreatedAt(), now.minusDays(1)));
        }

        if (createdAt == null) {
          createdAt = now.minusMinutes(1);
        }
        if (createdAt.isBefore(task.getCreatedAt())) {
          createdAt = task.getCreatedAt();
        }
        if (createdAt.isAfter(now.minusMinutes(1))) {
          createdAt = now.minusMinutes(1);
        }

        LocalDateTime updatedAt = createdAt.plusMinutes(20 + (long) random.nextInt(24) * 15L);
        if (updatedAt.isAfter(now.minusMinutes(1))) {
          updatedAt = now.minusMinutes(1);
        }
        if (updatedAt.isBefore(createdAt)) {
          updatedAt = createdAt;
        }

        schedules.add(
            TaskSchedule.builder()
                .task(task)
                .scheduledStart(start)
                .scheduledEnd(end)
                .scheduledDate(start.toLocalDate())
                .createdBy(creator)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build());

        if (scheduleIndex == targetSchedulesPerDay - 1) {
          break;
        }

        int remainingAfterCurrent = targetSchedulesPerDay - (scheduleIndex + 1);
        long minutesAfterCurrent = ChronoUnit.MINUTES.between(end, dayEnd);
        long minReserveForNext =
            (remainingAfterCurrent * 30L) + (Math.max(remainingAfterCurrent - 1, 0) * 15L);
        long maxGap = minutesAfterCurrent - minReserveForNext;

        if (maxGap < 15L) {
          break;
        }

        long boundedGapUpper = Math.min(45L, maxGap);
        long gapMinutes = 15L + random.nextInt((int) (boundedGapUpper - 15L + 1));
        cursor = ceilToQuarterHour(end.plusMinutes(gapMinutes));
      }
    }

    List<TaskSchedule> savedSchedules = taskScheduleRepository.saveAll(schedules);
    Map<Long, List<TaskSchedule>> schedulesByTaskId = new LinkedHashMap<>();
    for (TaskSchedule schedule : savedSchedules) {
      schedulesByTaskId
          .computeIfAbsent(schedule.getTask().getId(), key -> new ArrayList<>())
          .add(schedule);
    }
    for (List<TaskSchedule> value : schedulesByTaskId.values()) {
      value.sort(Comparator.comparing(TaskSchedule::getScheduledStart));
    }

    return new ScheduleSeedResult(savedSchedules, schedulesByTaskId);
  }

  private ReservedSlot reserveNearTermSlot(
      Random random,
      Map<Integer, List<LocalDateTime>> laneCursorsByDayOffset,
      int preferredDayOffset,
      LocalDateTime nowAnchor,
      TaskPriorityType priority) {
    long durationMinutes = resolveScheduleDurationMinutes(random, priority);

    for (int attempt = 0; attempt < 10; attempt++) {
      int dayOffset = Math.min(7, preferredDayOffset + Math.max(0, attempt - 1));
      List<LocalDateTime> laneCursors = laneCursorsByDayOffset.get(dayOffset);
      if (laneCursors == null || laneCursors.isEmpty()) {
        continue;
      }

      int laneIndex = random.nextInt(laneCursors.size());
      LocalDateTime laneStart = laneCursors.get(laneIndex);

      LocalDateTime start = ceilToQuarterHour(laneStart);
      if (dayOffset == 0) {
        start = maxDateTime(start, nowAnchor);
      }

      LocalDateTime dayEnd = laneStart.toLocalDate().atTime(18, 30);
      LocalDateTime end = start.plusMinutes(durationMinutes);

      if (end.isAfter(dayEnd)) {
        laneCursors.set(laneIndex, dayEnd.plusMinutes(15));
        continue;
      }

      laneCursors.set(laneIndex, end.plusMinutes(15));
      return new ReservedSlot(start, end);
    }

    return null;
  }

  private int pickNearTermDayOffset(Random random) {
    int[] weightedOffsets = {0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 4, 4, 5, 6, 7};
    return weightedOffsets[random.nextInt(weightedOffsets.length)];
  }

  private long resolveScheduleDurationMinutes(Random random, TaskPriorityType priority) {
    int[] durationOptions =
        switch (priority) {
          case URGENT -> new int[] {60, 75, 90, 105, 120};
          case HIGH -> new int[] {45, 60, 75, 90, 105};
          case MEDIUM -> new int[] {30, 45, 60, 75, 90};
          case LOW -> new int[] {30, 45, 60, 75};
        };
    return durationOptions[random.nextInt(durationOptions.length)];
  }

  private LocalDateTime ceilToQuarterHour(LocalDateTime value) {
    LocalDateTime normalized = value.withSecond(0).withNano(0);
    int minute = normalized.getMinute();
    int remainder = minute % 15;
    if (remainder == 0) {
      return normalized;
    }
    return normalized.plusMinutes(15 - remainder);
  }

  private CommentSeedResult seedTaskComments(
      Random random, LocalDateTime now, List<Task> tasks, MembershipSeedResult membershipSeed) {
    List<TaskComment> topLevelComments = new ArrayList<>();

    for (Task task : tasks) {
      int commentCount = resolveCommentCount(random, task);
      if (commentCount == 0) {
        continue;
      }

      List<User> participants;
      if (task.getProject().getVisibility() == ProjectVisibilityType.PRIVATE) {
        Set<User> allowed = new HashSet<>();
        allowed.add(task.getProject().getWorkspace().getOwner());
        allowed.add(task.getCreatedBy());
        if (task.getAssignee() != null) {
          allowed.add(task.getAssignee());
        }
        participants = new ArrayList<>(allowed);
      } else {
        participants =
            buildCommentParticipants(
                random,
                task,
                membershipSeed
                    .membersByWorkspaceId()
                    .getOrDefault(task.getProject().getWorkspace().getId(), List.of()));
      }

      LocalDateTime cursor = maxDateTime(task.getCreatedAt().plusHours(2), now.minusDays(180));
      for (int index = 0; index < commentCount; index++) {
        if (cursor.isAfter(now.minusMinutes(2))) {
          break;
        }

        User author = participants.get(random.nextInt(participants.size()));
        LocalDateTime createdAt = cursor.plusHours(1 + random.nextInt(30));
        if (createdAt.isAfter(now.minusMinutes(1))) {
          createdAt = now.minusMinutes(2 + random.nextInt(180));
        }
        if (createdAt.isBefore(task.getCreatedAt())) {
          createdAt = task.getCreatedAt().plusMinutes(5);
        }

        LocalDateTime updatedAt =
            random.nextDouble() < 0.22
                ? createdAt.plusMinutes(10 + random.nextInt(180))
                : createdAt;
        if (updatedAt.isAfter(now.minusMinutes(1))) {
          updatedAt = now.minusMinutes(1);
        }
        if (updatedAt.isBefore(createdAt)) {
          updatedAt = createdAt;
        }

        String content =
            COMMENT_TEMPLATES[
                (index + random.nextInt(COMMENT_TEMPLATES.length)) % COMMENT_TEMPLATES.length];
        topLevelComments.add(
            TaskComment.builder()
                .task(task)
                .user(author)
                .parentComment(null)
                .content(limitLength(content, 1000))
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build());

        cursor = createdAt;
      }
    }

    List<TaskComment> savedTopLevel = taskCommentRepository.saveAll(topLevelComments);
    List<TaskComment> replies = new ArrayList<>();

    for (TaskComment parent : savedTopLevel) {
      if (random.nextDouble() > 0.34) {
        continue;
      }

      List<User> workspaceMembers =
          membershipSeed
              .membersByWorkspaceId()
              .getOrDefault(parent.getTask().getProject().getWorkspace().getId(), List.of());
      User replier = pickDifferentUser(random, workspaceMembers, parent.getUser());
      if (replier == null) {
        continue;
      }

      LocalDateTime createdAt = parent.getCreatedAt().plusMinutes(15 + random.nextInt(240));
      if (createdAt.isAfter(now.minusMinutes(1))) {
        createdAt = now.minusMinutes(1 + random.nextInt(90));
      }
      if (createdAt.isBefore(parent.getCreatedAt())) {
        createdAt = parent.getCreatedAt().plusMinutes(1);
      }

      LocalDateTime updatedAt =
          random.nextDouble() < 0.15 ? createdAt.plusMinutes(10 + random.nextInt(60)) : createdAt;
      if (updatedAt.isAfter(now.minusMinutes(1))) {
        updatedAt = now.minusMinutes(1);
      }
      if (updatedAt.isBefore(createdAt)) {
        updatedAt = createdAt;
      }

      String replyContent = REPLY_TEMPLATES[random.nextInt(REPLY_TEMPLATES.length)];
      replies.add(
          TaskComment.builder()
              .task(parent.getTask())
              .user(replier)
              .parentComment(parent)
              .content(limitLength(replyContent, 1000))
              .createdAt(createdAt)
              .updatedAt(updatedAt)
              .build());
    }

    List<TaskComment> savedReplies = taskCommentRepository.saveAll(replies);
    List<TaskComment> allComments = new ArrayList<>(savedTopLevel);
    allComments.addAll(savedReplies);

    Map<Long, List<TaskComment>> commentsByTaskId = new LinkedHashMap<>();
    for (TaskComment comment : allComments) {
      commentsByTaskId
          .computeIfAbsent(comment.getTask().getId(), key -> new ArrayList<>())
          .add(comment);
    }
    for (List<TaskComment> value : commentsByTaskId.values()) {
      value.sort(Comparator.comparing(TaskComment::getCreatedAt));
    }

    return new CommentSeedResult(allComments, commentsByTaskId);
  }

  private void seedPomodoroSessions(
      Random random,
      LocalDateTime now,
      List<Task> tasks,
      MembershipSeedResult membershipSeed,
      ScheduleSeedResult scheduleSeed) {
    List<PomodoroSession> sessions = new ArrayList<>();

    for (Task task : tasks) {
      if (random.nextDouble() > 0.4) {
        continue;
      }

      User user = task.getAssignee();
      if (user == null) {
        user = task.getCreatedBy();
      }

      int sessionCount = 1 + random.nextInt(4);
      List<TaskSchedule> taskSchedules =
          scheduleSeed.schedulesByTaskId().getOrDefault(task.getId(), List.of());

      for (int i = 0; i < sessionCount; i++) {
        LocalDateTime createdAt;

        if (!taskSchedules.isEmpty()) {
          TaskSchedule schedule = taskSchedules.get(random.nextInt(taskSchedules.size()));
          LocalDateTime start = schedule.getScheduledStart();
          LocalDateTime end = schedule.getScheduledEnd();
          long minutes = ChronoUnit.MINUTES.between(start, end);

          if (minutes > 25) {
            createdAt = start.plusMinutes(random.nextInt((int) minutes - 25));
          } else {
            createdAt = start;
          }
        } else {
          LocalDateTime endLimit = task.getCompletedAt() != null ? task.getCompletedAt() : now;
          long minutesBetween = ChronoUnit.MINUTES.between(task.getCreatedAt(), endLimit);

          if (minutesBetween > 30) {
            long randomMinutes = 15 + random.nextLong(minutesBetween - 15);
            createdAt = task.getCreatedAt().plusMinutes(randomMinutes);
          } else {
            createdAt = task.getCreatedAt().plusMinutes(5 + random.nextInt(10));
          }
        }

        if (createdAt.isAfter(now)) {
          createdAt = now.minusMinutes(1);
        }

        sessions.add(
            PomodoroSession.builder()
                .task(task)
                .user(user)
                .durationMinutes(25)
                .createdAt(createdAt)
                .build());
      }
    }

    pomodoroSessionRepository.saveAll(sessions);
    log.info(">>> Seeded {} pomodoro sessions", sessions.size());
  }

  private List<WorkspaceInvite> seedWorkspaceInvites(
      Random random,
      LocalDateTime now,
      List<Workspace> workspaces,
      MembershipSeedResult membershipSeed) {
    List<WorkspaceInvite> invites = new ArrayList<>();
    Set<String> usedCodes = new HashSet<>();

    for (Workspace workspace : workspaces) {
      List<User> admins =
          membershipSeed.adminsByWorkspaceId().getOrDefault(workspace.getId(), List.of());
      User primaryCreator = admins.isEmpty() ? workspace.getOwner() : admins.get(0);
      User secondaryCreator = admins.size() > 1 ? admins.get(1) : primaryCreator;

      LocalDateTime createdBase =
          minDateTime(now.minusDays(20), workspace.getCreatedAt().plusDays(20));

      invites.add(
          WorkspaceInvite.builder()
              .workspace(workspace)
              .inviteCode(generateInviteCode(random, usedCodes, workspace.getName()))
              .roleToAssign(WorkspaceMemberRoleType.MEMBER)
              .createdBy(primaryCreator)
              .maxUses(null)
              .usedCount(0)
              .expiresAt(now.plusDays(45 + random.nextInt(45)))
              .isActive(true)
              .createdAt(createdBase.plusDays(random.nextInt(5)))
              .build());

      int usedCount = 2 + random.nextInt(4);
      invites.add(
          WorkspaceInvite.builder()
              .workspace(workspace)
              .inviteCode(generateInviteCode(random, usedCodes, workspace.getName()))
              .roleToAssign(WorkspaceMemberRoleType.MEMBER)
              .createdBy(secondaryCreator)
              .maxUses(10)
              .usedCount(usedCount)
              .expiresAt(now.plusDays(20 + random.nextInt(20)))
              .isActive(true)
              .createdAt(createdBase.plusDays(4 + random.nextInt(7)))
              .build());

      invites.add(
          WorkspaceInvite.builder()
              .workspace(workspace)
              .inviteCode(generateInviteCode(random, usedCodes, workspace.getName()))
              .roleToAssign(WorkspaceMemberRoleType.MEMBER)
              .createdBy(primaryCreator)
              .maxUses(1)
              .usedCount(1)
              .expiresAt(now.plusDays(7))
              .isActive(false)
              .createdAt(createdBase.plusDays(8 + random.nextInt(8)))
              .build());

      invites.add(
          WorkspaceInvite.builder()
              .workspace(workspace)
              .inviteCode(generateInviteCode(random, usedCodes, workspace.getName()))
              .roleToAssign(WorkspaceMemberRoleType.MEMBER)
              .createdBy(secondaryCreator)
              .maxUses(5)
              .usedCount(1 + random.nextInt(2))
              .expiresAt(now.plusDays(14))
              .isActive(false)
              .createdAt(createdBase.plusDays(10 + random.nextInt(10)))
              .build());

      if (random.nextDouble() < 0.65) {
        invites.add(
            WorkspaceInvite.builder()
                .workspace(workspace)
                .inviteCode(generateInviteCode(random, usedCodes, workspace.getName()))
                .roleToAssign(WorkspaceMemberRoleType.MEMBER)
                .createdBy(primaryCreator)
                .maxUses(3)
                .usedCount(1)
                .expiresAt(now.minusDays(1 + random.nextInt(10)))
                .isActive(false)
                .createdAt(createdBase.minusDays(8 + random.nextInt(8)))
                .build());
      }
    }

    return workspaceInviteRepository.saveAll(invites);
  }

  private List<Notification> seedNotifications(
      Random random,
      LocalDateTime now,
      List<WorkspaceMember> memberships,
      List<Task> tasks,
      Map<Long, List<TaskSchedule>> schedulesByTaskId,
      Map<Long, List<TaskComment>> commentsByTaskId,
      Map<Long, List<Goal>> goalsByProjectId,
      List<WorkspaceInvite> invites) {
    List<Notification> notifications = new ArrayList<>();

    for (WorkspaceMember member : memberships) {
      if (member.getRole() == WorkspaceMemberRoleType.OWNER) {
        continue;
      }
      LocalDateTime createdAt = member.getJoinedAt().plusMinutes(5 + random.nextInt(90));
      addNotification(
          notifications,
          random,
          now,
          member.getUser(),
          NotificationType.WORKSPACE_MEMBER_ADDED,
          "You were added to a workspace",
          "You were added to workspace "
              + member.getWorkspace().getName()
              + " as "
              + member.getRole()
              + ".",
          ReferenceType.WORKSPACE,
          member.getWorkspace().getId(),
          createdAt);
    }

    for (Task task : tasks) {
      if (task.getAssignee() != null
          && !task.getAssignee().getUserId().equals(task.getCreatedBy().getUserId())
          && random.nextDouble() < 0.92) {
        addNotification(
            notifications,
            random,
            now,
            task.getAssignee(),
            NotificationType.TASK_ASSIGNED,
            "New task assignment",
            "You were assigned to task " + task.getTitle() + ".",
            ReferenceType.TASK,
            task.getId(),
            task.getCreatedAt().plusMinutes(10 + random.nextInt(120)));
      }

      if (task.getAssignee() != null
          && task.getStatus().getPosition() >= 2
          && random.nextDouble() < 0.55) {
        addNotification(
            notifications,
            random,
            now,
            task.getAssignee(),
            NotificationType.TASK_STATUS_CHANGED,
            "Task status changed",
            "Task " + task.getTitle() + " moved to " + task.getStatus().getName() + ".",
            ReferenceType.TASK,
            task.getId(),
            maxDateTime(task.getCreatedAt().plusHours(1), task.getUpdatedAt().minusMinutes(45)));
      }

      List<TaskSchedule> taskSchedules = schedulesByTaskId.getOrDefault(task.getId(), List.of());
      if (!taskSchedules.isEmpty() && random.nextDouble() < 0.7) {
        TaskSchedule latestSchedule = taskSchedules.get(taskSchedules.size() - 1);
        User recipient = task.getAssignee() != null ? task.getAssignee() : task.getCreatedBy();
        addNotification(
            notifications,
            random,
            now,
            recipient,
            NotificationType.TASK_RESCHEDULED,
            "Task schedule updated",
            "Task " + task.getTitle() + " has a new schedule window.",
            ReferenceType.TASK,
            task.getId(),
            latestSchedule.getUpdatedAt());
      }

      List<TaskComment> taskComments = commentsByTaskId.getOrDefault(task.getId(), List.of());
      if (!taskComments.isEmpty() && random.nextDouble() < 0.82) {
        TaskComment firstComment = taskComments.get(0);
        User recipient = null;
        if (task.getAssignee() != null
            && !task.getAssignee().getUserId().equals(firstComment.getUser().getUserId())) {
          recipient = task.getAssignee();
        } else if (!task.getCreatedBy().getUserId().equals(firstComment.getUser().getUserId())) {
          recipient = task.getCreatedBy();
        }

        if (recipient != null) {
          addNotification(
              notifications,
              random,
              now,
              recipient,
              NotificationType.TASK_COMMENTED,
              "New comment on task",
              firstComment.getUser().getEmail() + " commented on task " + task.getTitle() + ".",
              ReferenceType.COMMENT,
              firstComment.getId(),
              firstComment.getCreatedAt().plusMinutes(2));
        }
      }
    }

    for (List<Goal> goals : goalsByProjectId.values()) {
      for (Goal goal : goals) {
        if (goal.getStatus() == GoalStatusType.NOT_STARTED || random.nextDouble() > 0.75) {
          continue;
        }
        User recipient =
            goal.getManagerUser() != null ? goal.getManagerUser() : goal.getCreatedBy();
        addNotification(
            notifications,
            random,
            now,
            recipient,
            NotificationType.GOAL_UPDATED,
            "Goal progress updated",
            "Goal " + goal.getTitle() + " is now at " + goal.getProgressPercent() + "% progress.",
            ReferenceType.GOAL,
            goal.getId(),
            goal.getUpdatedAt());
      }
    }

    for (WorkspaceInvite invite : invites) {
      if (invite.getUsedCount() <= 0) {
        continue;
      }
      addNotification(
          notifications,
          random,
          now,
          invite.getCreatedBy(),
          NotificationType.WORKSPACE_INVITE_USED,
          "Invite has been used",
          "An invite for workspace "
              + invite.getWorkspace().getName()
              + " was used by a new member.",
          ReferenceType.INVITE,
          invite.getId(),
          invite.getCreatedAt().plusDays(1 + random.nextInt(10)));
    }

    notifications.sort(Comparator.comparing(Notification::getCreatedAt));
    return notificationRepository.saveAll(notifications);
  }

  private List<ActivityLog> seedActivityLogs(
      Random random,
      LocalDateTime now,
      List<Workspace> workspaces,
      MembershipSeedResult membershipSeed,
      TeamSeedResult teamSeed,
      Map<Long, List<Project>> projectsByWorkspaceId,
      Map<Long, List<Goal>> goalsByProjectId,
      Map<Long, List<TaskType>> taskTypesByProjectId,
      List<Task> tasks,
      List<TaskSchedule> schedules,
      List<TaskComment> comments,
      List<WorkspaceInvite> invites) {
    List<ActivityLog> logs = new ArrayList<>();

    for (Workspace workspace : workspaces) {
      addActivityLog(
          logs,
          now,
          workspace,
          workspace.getOwner(),
          ActivityActionType.WORKSPACE_CREATED,
          ActivityTargetType.WORKSPACE,
          workspace.getId(),
          "Create workspace " + workspace.getName(),
          workspace.getCreatedAt().plusMinutes(2));
    }

    for (WorkspaceMember member : membershipSeed.memberships()) {
      Workspace workspace = member.getWorkspace();
      List<User> admins =
          membershipSeed.adminsByWorkspaceId().getOrDefault(workspace.getId(), List.of());
      User actor =
          admins.isEmpty() ? workspace.getOwner() : admins.get(random.nextInt(admins.size()));

      if (member.getRole() != WorkspaceMemberRoleType.OWNER) {
        addActivityLog(
            logs,
            now,
            workspace,
            actor,
            ActivityActionType.MEMBER_ADDED,
            ActivityTargetType.MEMBER,
            member.getId(),
            "Add member " + member.getUser().getEmail() + " to workspace",
            member.getJoinedAt().plusMinutes(4 + random.nextInt(40)));
      }

      if (member.getRole() == WorkspaceMemberRoleType.MEMBER && random.nextDouble() < 0.45) {
        addActivityLog(
            logs,
            now,
            workspace,
            workspace.getOwner(),
            ActivityActionType.MEMBER_ROLE_UPDATED,
            ActivityTargetType.MEMBER,
            member.getId(),
            "Update role for member " + member.getUser().getEmail(),
            member.getJoinedAt().plusDays(2 + random.nextInt(10)));
      }
    }

    for (WorkspaceTeam team : teamSeed.teams()) {
      addActivityLog(
          logs,
          now,
          team.getWorkspace(),
          team.getCreatedBy(),
          ActivityActionType.TEAM_CREATED,
          ActivityTargetType.TEAM,
          team.getId(),
          "Create team " + team.getName(),
          team.getCreatedAt());
    }

    for (WorkspaceTeamMember teamMember : teamSeed.teamMembers()) {
      WorkspaceTeam team = teamMember.getTeam();
      addActivityLog(
          logs,
          now,
          team.getWorkspace(),
          team.getCreatedBy(),
          ActivityActionType.TEAM_MEMBER_ADDED,
          ActivityTargetType.TEAM,
          team.getId(),
          "Add member " + teamMember.getUser().getEmail() + " to team " + team.getName(),
          teamMember.getJoinedAt().plusMinutes(3 + random.nextInt(20)));
    }

    for (Map.Entry<Long, List<Project>> entry : projectsByWorkspaceId.entrySet()) {
      for (Project project : entry.getValue()) {
        addActivityLog(
            logs,
            now,
            project.getWorkspace(),
            project.getCreatedBy(),
            ActivityActionType.PROJECT_CREATED,
            ActivityTargetType.PROJECT,
            project.getId(),
            "Create project " + project.getName(),
            project.getCreatedAt());

        if (project.getUpdatedAt().isAfter(project.getCreatedAt().plusDays(4))) {
          addActivityLog(
              logs,
              now,
              project.getWorkspace(),
              project.getManagerUser() != null ? project.getManagerUser() : project.getCreatedBy(),
              ActivityActionType.PROJECT_UPDATED,
              ActivityTargetType.PROJECT,
              project.getId(),
              "Update project " + project.getName(),
              project.getUpdatedAt());
        }
      }
    }

    for (List<TaskType> taskTypes : taskTypesByProjectId.values()) {
      for (TaskType taskType : taskTypes) {
        addActivityLog(
            logs,
            now,
            taskType.getWorkspace(),
            taskType.getProject().getCreatedBy(),
            ActivityActionType.TASK_TYPE_CREATED,
            ActivityTargetType.TASK_TYPE,
            taskType.getId(),
            "Create task type " + taskType.getName(),
            taskType.getCreatedAt());

        if (taskType.getUpdatedAt().isAfter(taskType.getCreatedAt().plusDays(2))
            && random.nextDouble() < 0.35) {
          addActivityLog(
              logs,
              now,
              taskType.getWorkspace(),
              taskType.getProject().getCreatedBy(),
              ActivityActionType.TASK_TYPE_UPDATED,
              ActivityTargetType.TASK_TYPE,
              taskType.getId(),
              "Update task type " + taskType.getName(),
              taskType.getUpdatedAt());
        }
      }
    }

    for (List<Goal> goals : goalsByProjectId.values()) {
      for (Goal goal : goals) {
        Workspace workspace = goal.getProject().getWorkspace();
        addActivityLog(
            logs,
            now,
            workspace,
            goal.getCreatedBy(),
            ActivityActionType.GOAL_CREATED,
            ActivityTargetType.GOAL,
            goal.getId(),
            "Create goal " + goal.getTitle(),
            goal.getCreatedAt());

        if (goal.getUpdatedAt().isAfter(goal.getCreatedAt().plusHours(12))) {
          addActivityLog(
              logs,
              now,
              workspace,
              goal.getManagerUser() != null ? goal.getManagerUser() : goal.getCreatedBy(),
              ActivityActionType.GOAL_UPDATED,
              ActivityTargetType.GOAL,
              goal.getId(),
              "Update goal " + goal.getTitle(),
              goal.getUpdatedAt());
        }
      }
    }

    for (Task task : tasks) {
      Workspace workspace = task.getProject().getWorkspace();
      addActivityLog(
          logs,
          now,
          workspace,
          task.getCreatedBy(),
          ActivityActionType.TASK_CREATED,
          ActivityTargetType.TASK,
          task.getId(),
          "Create task " + task.getTitle(),
          task.getCreatedAt());

      if (task.getAssignee() != null
          && !task.getAssignee().getUserId().equals(task.getCreatedBy().getUserId())) {
        addActivityLog(
            logs,
            now,
            workspace,
            task.getCreatedBy(),
            ActivityActionType.TASK_ASSIGNED,
            ActivityTargetType.TASK,
            task.getId(),
            "Assign task " + task.getTitle() + " to " + task.getAssignee().getEmail(),
            task.getCreatedAt().plusMinutes(10 + random.nextInt(90)));
      }

      if (task.getStatus().getPosition() >= 2) {
        addActivityLog(
            logs,
            now,
            workspace,
            task.getAssignee() != null ? task.getAssignee() : task.getCreatedBy(),
            ActivityActionType.TASK_MOVED_STATUS,
            ActivityTargetType.TASK,
            task.getId(),
            "Move task " + task.getTitle() + " to " + task.getStatus().getName(),
            maxDateTime(task.getCreatedAt().plusHours(1), task.getUpdatedAt().minusMinutes(20)));
      }

      if (task.getUpdatedAt().isAfter(task.getCreatedAt().plusHours(8))
          && random.nextDouble() < 0.28) {
        addActivityLog(
            logs,
            now,
            workspace,
            task.getCreatedBy(),
            ActivityActionType.TASK_UPDATED,
            ActivityTargetType.TASK,
            task.getId(),
            "Update task " + task.getTitle(),
            task.getUpdatedAt());
      }
    }

    for (TaskSchedule schedule : schedules) {
      Task task = schedule.getTask();
      addActivityLog(
          logs,
          now,
          task.getProject().getWorkspace(),
          schedule.getCreatedBy(),
          ActivityActionType.TASK_RESCHEDULED,
          ActivityTargetType.SCHEDULE,
          schedule.getId(),
          "Create or update schedule for task " + task.getTitle(),
          schedule.getUpdatedAt());
    }

    for (TaskComment comment : comments) {
      if (random.nextDouble() > 0.4) {
        continue;
      }

      Task task = comment.getTask();
      addActivityLog(
          logs,
          now,
          task.getProject().getWorkspace(),
          comment.getUser(),
          ActivityActionType.COMMENT_ADDED,
          ActivityTargetType.COMMENT,
          comment.getId(),
          "Add comment to task " + task.getTitle(),
          comment.getCreatedAt());

      if (comment.getUpdatedAt().isAfter(comment.getCreatedAt().plusMinutes(5))) {
        addActivityLog(
            logs,
            now,
            task.getProject().getWorkspace(),
            comment.getUser(),
            ActivityActionType.COMMENT_UPDATED,
            ActivityTargetType.COMMENT,
            comment.getId(),
            "Update comment on task " + task.getTitle(),
            comment.getUpdatedAt());
      }
    }

    for (WorkspaceInvite invite : invites) {
      Workspace workspace = invite.getWorkspace();
      addActivityLog(
          logs,
          now,
          workspace,
          invite.getCreatedBy(),
          ActivityActionType.INVITE_CREATED,
          ActivityTargetType.INVITE,
          invite.getId(),
          "Create invite for workspace " + workspace.getName(),
          invite.getCreatedAt());

      if (invite.getUsedCount() > 0) {
        List<User> members =
            membershipSeed.membersByWorkspaceId().getOrDefault(workspace.getId(), List.of());
        User actor = pickDifferentUser(random, members, invite.getCreatedBy());
        if (actor == null) {
          actor = invite.getCreatedBy();
        }

        addActivityLog(
            logs,
            now,
            workspace,
            actor,
            ActivityActionType.INVITE_USED,
            ActivityTargetType.INVITE,
            invite.getId(),
            "Use invite for workspace " + workspace.getName(),
            invite.getCreatedAt().plusDays(1 + random.nextInt(7)));
      }

      if (!Boolean.TRUE.equals(invite.getIsActive())
          && (invite.getUsedCount() == 0
              || (invite.getMaxUses() != null && invite.getUsedCount() < invite.getMaxUses()))) {
        LocalDateTime revokeTime =
            invite.getExpiresAt() != null
                ? invite.getExpiresAt()
                : invite.getCreatedAt().plusDays(5);
        addActivityLog(
            logs,
            now,
            workspace,
            invite.getCreatedBy(),
            ActivityActionType.INVITE_REVOKED,
            ActivityTargetType.INVITE,
            invite.getId(),
            "Revoke invite for workspace " + workspace.getName(),
            revokeTime);
      }
    }

    logs.sort(Comparator.comparing(ActivityLog::getCreatedAt));
    return activityLogRepository.saveAll(logs);
  }

  private void addNotification(
      List<Notification> notifications,
      Random random,
      LocalDateTime now,
      User recipient,
      NotificationType type,
      String title,
      String message,
      ReferenceType referenceType,
      Long referenceId,
      LocalDateTime createdAt) {
    if (recipient == null || referenceId == null || createdAt == null) {
      return;
    }

    LocalDateTime safeCreatedAt = createdAt.isAfter(now) ? now.minusMinutes(1) : createdAt;
    notifications.add(
        Notification.builder()
            .user(recipient)
            .type(type)
            .title(limitLength(title, 200))
            .message(limitLength(message, 2000))
            .referenceType(referenceType)
            .referenceId(referenceId)
            .isRead(resolveReadState(random, now, safeCreatedAt))
            .createdAt(safeCreatedAt)
            .build());
  }

  private void addActivityLog(
      List<ActivityLog> logs,
      LocalDateTime now,
      Workspace workspace,
      User actor,
      ActivityActionType actionType,
      ActivityTargetType targetType,
      Long targetId,
      String description,
      LocalDateTime createdAt) {
    if (workspace == null || actor == null || targetId == null || createdAt == null) {
      return;
    }

    LocalDateTime safeCreatedAt = createdAt.isAfter(now) ? now.minusMinutes(1) : createdAt;
    logs.add(
        ActivityLog.builder()
            .workspace(workspace)
            .actor(actor)
            .actionType(actionType)
            .targetType(targetType)
            .targetId(targetId)
            .description(limitLength(description, 255))
            .createdAt(safeCreatedAt)
            .build());
  }

  private User pickManagerUser(
      Random random,
      WorkspaceTeam managerTeam,
      Workspace workspace,
      MembershipSeedResult membershipSeed,
      TeamSeedResult teamSeed) {
    if (managerTeam != null) {
      List<User> teamMembers =
          teamSeed.membersByTeamId().getOrDefault(managerTeam.getId(), List.of());
      if (!teamMembers.isEmpty()) {
        return teamMembers.get(random.nextInt(teamMembers.size()));
      }
    }

    List<User> admins =
        membershipSeed.adminsByWorkspaceId().getOrDefault(workspace.getId(), List.of());
    if (!admins.isEmpty()) {
      return admins.get(random.nextInt(admins.size()));
    }

    return workspace.getOwner();
  }

  private LocalDateTime resolveProjectUpdatedAt(
      Random random, LocalDateTime now, LocalDateTime createdAt, ProjectStatusType status) {
    if (status == ProjectStatusType.COMPLETED) {
      // Fix 3: Ensure some projects are completed very recently for 7-day completion trend charts
      LocalDateTime lowerBound = now.minusDays(6);
      LocalDateTime upperBound = now.minusHours(2);
      if (lowerBound.isBefore(createdAt)) {
        lowerBound = createdAt.plusHours(1);
      }
      return randomDateTimeBetween(random, lowerBound, upperBound);
    }

    LocalDateTime lowerBound = createdAt.plusDays(5);
    LocalDateTime upperBound =
        switch (status) {
          case ACTIVE -> now.minusDays(1);
          case COMPLETED -> now.minusDays(2); // Fallback
          case ARCHIVED -> now.minusDays(20);
        };

    if (upperBound.isBefore(lowerBound)) {
      upperBound = now.minusDays(1);
    }

    return randomDateTimeBetween(random, lowerBound, upperBound);
  }

  private int resolveGoalCount(ProjectStatusType status, Random random) {
    return switch (status) {
      case ACTIVE -> 3 + random.nextInt(2);
      case COMPLETED -> 3 + random.nextInt(2);
      case ARCHIVED -> 3 + random.nextInt(2);
    };
  }

  private List<String> buildGoalTitles(Project project) {
    String projectTag = extractProjectTag(project.getName());
    return List.of(
        "Reduce onboarding friction and increase activation for " + projectTag,
        "Stabilize weekly delivery quality for " + projectTag,
        "Cut critical production defects for " + projectTag,
        "Improve sprint predictability and execution confidence",
        "Complete permission and ownership edge-case coverage",
        "Ship clear team handover documentation and runbook updates");
  }

  private GoalStatusType chooseGoalStatus(ProjectStatusType projectStatus, Random random) {
    int roll = random.nextInt(100);
    return switch (projectStatus) {
      case ACTIVE -> {
        if (roll < 20) yield GoalStatusType.NOT_STARTED;
        if (roll < 72) yield GoalStatusType.IN_PROGRESS;
        if (roll < 87) yield GoalStatusType.COMPLETED;
        yield GoalStatusType.ON_HOLD;
      }
      case COMPLETED -> {
        if (roll < 72) yield GoalStatusType.COMPLETED;
        if (roll < 88) yield GoalStatusType.IN_PROGRESS;
        yield GoalStatusType.ON_HOLD;
      }
      case ARCHIVED -> {
        if (roll < 48) yield GoalStatusType.ON_HOLD;
        if (roll < 76) yield GoalStatusType.COMPLETED;
        yield GoalStatusType.IN_PROGRESS;
      }
    };
  }

  private WorkspaceTeam chooseGoalManagerTeam(
      Random random, Project project, List<WorkspaceTeam> workspaceTeams) {
    if (workspaceTeams.isEmpty()) {
      return null;
    }
    if (project.getManagerTeam() != null && random.nextDouble() < 0.62) {
      return project.getManagerTeam();
    }
    return workspaceTeams.get(random.nextInt(workspaceTeams.size()));
  }

  private User chooseGoalManagerUser(
      Random random,
      Project project,
      WorkspaceTeam managerTeam,
      List<User> workspaceMembers,
      MembershipSeedResult membershipSeed,
      TeamSeedResult teamSeed) {
    if (managerTeam != null) {
      List<User> teamMembers =
          teamSeed.membersByTeamId().getOrDefault(managerTeam.getId(), List.of());
      if (!teamMembers.isEmpty()) {
        return teamMembers.get(random.nextInt(teamMembers.size()));
      }
    }

    if (project.getManagerUser() != null && random.nextDouble() < 0.75) {
      return project.getManagerUser();
    }

    List<User> admins =
        membershipSeed
            .adminsByWorkspaceId()
            .getOrDefault(project.getWorkspace().getId(), List.of());
    if (!admins.isEmpty()) {
      return admins.get(random.nextInt(admins.size()));
    }

    if (!workspaceMembers.isEmpty()) {
      return workspaceMembers.get(random.nextInt(workspaceMembers.size()));
    }

    return project.getCreatedBy();
  }

  private BigDecimal progressForGoalStatus(GoalStatusType status, Random random) {
    BigDecimal progress =
        switch (status) {
          case NOT_STARTED -> BigDecimal.ZERO;
          case IN_PROGRESS -> BigDecimal.valueOf(25 + random.nextInt(60));
          case COMPLETED -> BigDecimal.valueOf(100);
          case ON_HOLD -> BigDecimal.valueOf(20 + random.nextInt(55));
        };
    return progress.setScale(2, RoundingMode.HALF_UP);
  }

  private int resolveTaskCount(Project project, Random random) {
    int base =
        switch (project.getStatus()) {
          case ACTIVE -> 32 + random.nextInt(24);
          case COMPLETED -> 22 + random.nextInt(18);
          case ARCHIVED -> 12 + random.nextInt(12);
        };

    double[] factors = {0.75, 0.95, 1.1, 1.35};
    double factor = factors[random.nextInt(factors.length)];
    if (project.getWorkspace().getName().contains("Product")) {
      factor += 0.15;
    }

    return Math.max(10, (int) Math.round(base * factor));
  }

  private TaskStatus chooseTaskStatus(
      Random random, ProjectStatusType projectStatus, List<TaskStatus> statuses) {
    int roll = random.nextInt(100);
    return switch (projectStatus) {
      case ACTIVE -> {
        if (roll < 18) yield statuses.get(0);
        if (roll < 40) yield statuses.get(1);
        if (roll < 68) yield statuses.get(2);
        if (roll < 84) yield statuses.get(3);
        yield statuses.get(4);
      }
      case COMPLETED -> {
        if (roll < 90) yield statuses.get(4); // Done
        yield statuses.get(0); // Backlog (Cancelled)
      }
      case ARCHIVED -> {
        if (roll < 95) yield statuses.get(4); // Done
        yield statuses.get(0); // Backlog (Cancelled)
      }
    };
  }

  private Goal chooseGoalForTask(Random random, List<Goal> goals) {
    if (goals.isEmpty() || random.nextDouble() < 0.28) {
      return null;
    }
    return goals.get(random.nextInt(goals.size()));
  }

  private TaskType chooseTaskTypeForTask(Random random, List<TaskType> taskTypes, Goal goal) {
    if (taskTypes.isEmpty()) {
      return null;
    }

    if (goal != null && random.nextDouble() < 0.65) {
      List<TaskType> goalScopedTypes = new ArrayList<>();
      for (TaskType taskType : taskTypes) {
        if (taskType.getGoal() != null && taskType.getGoal().getId().equals(goal.getId())) {
          goalScopedTypes.add(taskType);
        }
      }
      if (!goalScopedTypes.isEmpty()) {
        return goalScopedTypes.get(random.nextInt(goalScopedTypes.size()));
      }
    }

    return taskTypes.get(random.nextInt(taskTypes.size()));
  }

  private TaskPriorityType choosePriority(Random random) {
    int roll = random.nextInt(100);
    if (roll < 20) return TaskPriorityType.LOW;
    if (roll < 60) return TaskPriorityType.MEDIUM;
    if (roll < 87) return TaskPriorityType.HIGH;
    return TaskPriorityType.URGENT;
  }

  private SourceViewType chooseSourceView(Random random) {
    int roll = random.nextInt(100);
    if (roll < 64) return SourceViewType.KANBAN;
    if (roll < 85) return SourceViewType.TODO;
    return SourceViewType.CALENDAR;
  }

  private LocalDateTime resolveTaskDueDate(
      Random random,
      LocalDateTime now,
      LocalDateTime createdAt,
      boolean completed,
      ProjectStatusType projectStatus) {
    if (random.nextDouble() < 0.18) {
      return null;
    }

    if (completed) {
      LocalDateTime dueDate = createdAt.plusDays(3 + random.nextInt(25));
      if (dueDate.isAfter(now.minusHours(2))) {
        dueDate = now.minusDays(1 + random.nextInt(5));
      }
      return dueDate;
    }

    boolean overdue =
        random.nextDouble() < (projectStatus == ProjectStatusType.ACTIVE ? 0.24 : 0.18);
    if (overdue) {
      LocalDateTime dueDate = now.minusDays(1 + random.nextInt(14));
      if (dueDate.isBefore(createdAt.plusHours(2))) {
        dueDate = createdAt.plusDays(1 + random.nextInt(6));
      }
      return dueDate;
    }

    LocalDateTime dueDate = now.plusDays(2 + random.nextInt(35));
    if (dueDate.isBefore(createdAt.plusHours(2))) {
      dueDate = createdAt.plusDays(2 + random.nextInt(8));
    }
    return dueDate;
  }

  private LocalDateTime resolveTaskCompletedAt(
      Random random, LocalDateTime now, LocalDateTime createdAt, LocalDateTime dueDate) {
    LocalDateTime earliest = createdAt.plusHours(2);
    LocalDateTime latest = now.minusMinutes(30);

    LocalDateTime candidate;
    if (dueDate != null) {
      candidate = dueDate.plusDays(random.nextInt(5) - 2L);
    } else {
      candidate = createdAt.plusDays(1 + random.nextInt(20));
    }

    return clampDateTime(candidate, earliest, latest);
  }

  private LocalDateTime resolveTaskUpdatedAt(
      Random random, LocalDateTime now, LocalDateTime createdAt, LocalDateTime completedAt) {
    LocalDateTime lowerBound = completedAt != null ? completedAt : createdAt;
    LocalDateTime upperBound = now.minusMinutes(1);

    if (upperBound.isBefore(lowerBound)) {
      return lowerBound;
    }

    return randomDateTimeBetween(random, lowerBound, upperBound);
  }

  private String generateTaskTitle(Random random, Project project, Goal goal) {
    String action = TASK_ACTIONS[random.nextInt(TASK_ACTIONS.length)];
    String object = TASK_OBJECTS[random.nextInt(TASK_OBJECTS.length)];
    String context = TASK_CONTEXTS[random.nextInt(TASK_CONTEXTS.length)];
    String projectTag = extractProjectTag(project.getName());

    String title = action + " " + object + " for " + projectTag;
    if (goal != null && random.nextDouble() < 0.40) {
      title += " (" + goal.getGoalType() + ")";
    }
    if (random.nextDouble() < 0.35) {
      title += " " + context;
    }

    return title;
  }

  private String buildTaskDescription(String title, Project project, Goal goal) {
    StringBuilder builder = new StringBuilder();
    builder.append("Scope: ").append(title).append(". ");
    builder.append("Project context: ").append(project.getName()).append(". ");
    if (goal != null) {
      builder.append("Linked goal: ").append(goal.getTitle()).append(". ");
    }
    builder.append(
        "Acceptance: clear owner, measurable outcome, and updated runbook after delivery.");
    return limitLength(builder.toString(), 5000);
  }

  private String buildTaskNotesHtml(Project project, Random random) {
    String bulletOne =
        random.nextBoolean()
            ? "Prepare rollout checklist for internal QA"
            : "Align with stakeholders on acceptance criteria";
    String bulletTwo =
        random.nextBoolean()
            ? "Capture known limitations and fallback path"
            : "Document monitoring metrics and alert thresholds";

    return "<p><strong>Working notes</strong> for "
        + project.getName()
        + "</p>"
        + "<ul><li>"
        + bulletOne
        + "</li><li>"
        + bulletTwo
        + "</li></ul>";
  }

  private int estimateMinutes(TaskPriorityType priority, Random random) {
    return switch (priority) {
      case LOW -> 60 * (1 + random.nextInt(4));
      case MEDIUM -> 60 * (2 + random.nextInt(6));
      case HIGH -> 60 * (4 + random.nextInt(7));
      case URGENT -> 60 * (3 + random.nextInt(6));
    };
  }

  private int resolveCommentCount(Random random, Task task) {
    int min = 3;
    int max = 5;

    if (task.getPriority() == TaskPriorityType.URGENT
        || task.getPriority() == TaskPriorityType.HIGH) {
      min = 4;
    }

    int count = min + random.nextInt(max - min + 1);
    if (Boolean.TRUE.equals(task.getIsCompleted()) && random.nextDouble() < 0.15 && count < 5) {
      count++;
    }

    return Math.min(5, count);
  }

  private List<User> buildCommentParticipants(
      Random random, Task task, List<User> workspaceMembers) {
    LinkedHashSet<User> participants = new LinkedHashSet<>();
    participants.add(task.getCreatedBy());
    if (task.getAssignee() != null) {
      participants.add(task.getAssignee());
    }

    if (workspaceMembers.isEmpty()) {
      return new ArrayList<>(participants);
    }

    List<User> orderedMembers = new ArrayList<>(workspaceMembers);
    orderedMembers.sort(Comparator.comparing(user -> nullableLower(user.getEmail())));

    User anchor = task.getAssignee() != null ? task.getAssignee() : task.getCreatedBy();
    int anchorIndex = 0;
    for (int index = 0; index < orderedMembers.size(); index++) {
      if (orderedMembers.get(index).getUserId().equals(anchor.getUserId())) {
        anchorIndex = index;
        break;
      }
    }

    List<User> closeCollaborators = buildCollaborationCandidates(orderedMembers, anchorIndex, 7);
    int targetSize = Math.min(orderedMembers.size(), 3 + random.nextInt(3));
    targetSize = Math.max(targetSize, Math.min(orderedMembers.size(), participants.size()));
    for (User collaborator : closeCollaborators) {
      if (participants.size() >= targetSize) {
        break;
      }
      participants.add(collaborator);
    }

    while (participants.size() < targetSize) {
      participants.add(orderedMembers.get(random.nextInt(orderedMembers.size())));
    }

    return new ArrayList<>(participants);
  }

  private List<User> buildCollaborationCandidates(
      List<User> orderedMembers, int anchorIndex, int maxCandidates) {
    if (orderedMembers == null || orderedMembers.isEmpty() || maxCandidates <= 0) {
      return List.of();
    }

    int size = orderedMembers.size();
    LinkedHashSet<User> candidates = new LinkedHashSet<>();
    int radius = 1;
    while (candidates.size() < maxCandidates && radius < size) {
      int left = Math.floorMod(anchorIndex - radius, size);
      int right = Math.floorMod(anchorIndex + radius, size);

      candidates.add(orderedMembers.get(left));
      if (candidates.size() >= maxCandidates) {
        break;
      }
      candidates.add(orderedMembers.get(right));
      radius++;
    }

    User anchor = orderedMembers.get(Math.floorMod(anchorIndex, size));
    candidates.removeIf(user -> user.getUserId().equals(anchor.getUserId()));
    return new ArrayList<>(candidates);
  }

  private User pickDifferentUser(Random random, List<User> candidates, User excluded) {
    if (candidates == null || candidates.isEmpty()) {
      return null;
    }

    List<User> filtered = new ArrayList<>();
    for (User candidate : candidates) {
      if (candidate != null
          && (excluded == null || !candidate.getUserId().equals(excluded.getUserId()))) {
        filtered.add(candidate);
      }
    }
    if (filtered.isEmpty()) {
      return null;
    }
    return filtered.get(random.nextInt(filtered.size()));
  }

  private boolean resolveReadState(Random random, LocalDateTime now, LocalDateTime createdAt) {
    long ageDays = Math.max(0, ChronoUnit.DAYS.between(createdAt, now));
    double readProbability;
    if (ageDays >= 60) {
      readProbability = 0.94;
    } else if (ageDays >= 30) {
      readProbability = 0.85;
    } else if (ageDays >= 7) {
      readProbability = 0.68;
    } else {
      readProbability = 0.42;
    }
    return random.nextDouble() < readProbability;
  }

  private String generateInviteCode(Random random, Set<String> usedCodes, String workspaceName) {
    String initials = workspaceInitials(workspaceName);
    String code;
    do {
      code = initials + randomAlphaNumeric(random, 8);
      if (code.length() > 20) {
        code = code.substring(0, 20);
      }
    } while (!usedCodes.add(code));
    return code;
  }

  private String workspaceInitials(String workspaceName) {
    if (workspaceName == null || workspaceName.isBlank()) {
      return "WS";
    }

    String[] parts = workspaceName.trim().split("\\s+");
    StringBuilder builder = new StringBuilder();
    for (String part : parts) {
      if (!part.isBlank() && Character.isLetter(part.charAt(0))) {
        builder.append(Character.toUpperCase(part.charAt(0)));
      }
      if (builder.length() >= 3) {
        break;
      }
    }

    String value = builder.length() == 0 ? "WS" : builder.toString();
    return value.length() < 2 ? value + "X" : value;
  }

  private String randomAlphaNumeric(Random random, int length) {
    char[] alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    StringBuilder builder = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      builder.append(alphabet[random.nextInt(alphabet.length)]);
    }
    return builder.toString();
  }

  private String extractProjectTag(String projectName) {
    if (projectName == null || projectName.isBlank()) {
      return "project";
    }

    String[] parts = projectName.trim().split("\\s+");
    if (parts.length == 1) {
      return parts[0];
    }
    return parts[0] + " " + parts[1];
  }

  private String generateUniqueEmail(String firstName, String lastName, Set<String> usedEmails) {
    String firstToken = normalizeToken(firstName);
    String lastToken = normalizeToken(lastName);
    String base = firstToken + "." + lastToken;

    String candidate = base + "@" + SEED_EMAIL_DOMAIN;
    int suffix = 2;
    while (!usedEmails.add(candidate.toLowerCase(Locale.ROOT))) {
      candidate = base + suffix + "@" + SEED_EMAIL_DOMAIN;
      suffix++;
    }
    return candidate;
  }

  private String generateUniqueVietnamPhone(Random random, Set<String> usedPhones) {
    String candidate;
    do {
      String prefix = VN_PHONE_PREFIXES[random.nextInt(VN_PHONE_PREFIXES.length)];
      String suffix = String.format(Locale.ROOT, "%07d", random.nextInt(10_000_000));
      candidate = prefix + suffix;
    } while (!usedPhones.add(candidate));
    return candidate;
  }

  private boolean isReservedInitAccountEmail(String email) {
    if (email == null) {
      return false;
    }
    return RESERVED_INIT_EMAILS.contains(email.toLowerCase(Locale.ROOT));
  }

  private boolean isAllowedSeedEmail(String email) {
    if (email == null || email.isBlank()) {
      return false;
    }
    return email.toLowerCase(Locale.ROOT).endsWith("@" + SEED_EMAIL_DOMAIN);
  }

  private boolean isValidSeedPhone(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.isBlank()) {
      return false;
    }
    return VN_PHONE_PATTERN.matcher(phoneNumber).matches();
  }

  private String normalizeToken(String value) {
    if (value == null || value.isBlank()) {
      return "user";
    }

    String normalized =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase(Locale.ROOT);
    normalized = NON_ASCII_TOKEN.matcher(normalized).replaceAll("");
    if (normalized.isBlank()) {
      return "user";
    }
    return normalized.length() > 20 ? normalized.substring(0, 20) : normalized;
  }

  private LocalDateTime randomDateTimeBetween(Random random, LocalDateTime from, LocalDateTime to) {
    if (from == null && to == null) {
      return LocalDateTime.now();
    }
    if (from == null) {
      return to;
    }
    if (to == null || to.isBefore(from)) {
      return from;
    }

    long totalSeconds = ChronoUnit.SECONDS.between(from, to);
    if (totalSeconds <= 0) {
      return from;
    }

    long offsetSeconds = random.nextLong(totalSeconds + 1);
    return from.plusSeconds(offsetSeconds);
  }

  private LocalDateTime clampDateTime(LocalDateTime value, LocalDateTime min, LocalDateTime max) {
    if (value == null) {
      return min;
    }
    if (min != null && value.isBefore(min)) {
      return min;
    }
    if (max != null && value.isAfter(max)) {
      return max;
    }
    return value;
  }

  private LocalDateTime minDateTime(LocalDateTime left, LocalDateTime right) {
    if (left == null) return right;
    if (right == null) return left;
    return left.isBefore(right) ? left : right;
  }

  private LocalDateTime maxDateTime(LocalDateTime left, LocalDateTime right) {
    if (left == null) return right;
    if (right == null) return left;
    return left.isAfter(right) ? left : right;
  }

  private String nullableLower(String value) {
    return value == null ? "" : value.toLowerCase(Locale.ROOT);
  }

  private String limitLength(String value, int maxLength) {
    if (value == null) {
      return null;
    }
    return value.length() > maxLength ? value.substring(0, maxLength) : value;
  }

  private List<TaskDependency> seedTaskDependencies(
      Random random, LocalDateTime now, List<Task> tasks) {
    List<TaskDependency> dependencies = new ArrayList<>();
    Map<Long, List<Task>> tasksByProjectId =
        tasks.stream().collect(java.util.stream.Collectors.groupingBy(t -> t.getProject().getId()));

    for (Map.Entry<Long, List<Task>> entry : tasksByProjectId.entrySet()) {
      List<Task> projectTasks = entry.getValue();

      // Group by phase
      List<Task> planningTasks = new ArrayList<>();
      List<Task> designTasks = new ArrayList<>();
      List<Task> implTasks = new ArrayList<>();
      List<Task> testTasks = new ArrayList<>();
      List<Task> deployTasks = new ArrayList<>();

      for (Task t : projectTasks) {
        String type = t.getTaskType().getName();
        String name = t.getTitle().toLowerCase();

        if (type.equals("Research") || name.contains("plan") || name.contains("discovery")) {
          planningTasks.add(t);
        } else if (type.equals("Design") || name.contains("mockup") || name.contains("ui/ux")) {
          designTasks.add(t);
        } else if (type.equals("Feature") || type.equals("Bug") || type.equals("Improvement")) {
          implTasks.add(t);
        } else if (type.equals("Testing") || type.equals("Review") || name.contains("qa")) {
          testTasks.add(t);
        } else if (type.equals("Deployment")
            || type.equals("Operations")
            || name.contains("release")) {
          deployTasks.add(t);
        } else {
          implTasks.add(t); // fallback
        }
      }

      // Create dependencies between phases
      createDependenciesBetweenPhases(
          random,
          now,
          dependencies,
          designTasks,
          planningTasks,
          0.7); // 70% design depends on planning
      createDependenciesBetweenPhases(
          random, now, dependencies, implTasks, designTasks, 0.6); // 60% impl depends on design
      createDependenciesBetweenPhases(
          random, now, dependencies, testTasks, implTasks, 0.8); // 80% test depends on impl
      createDependenciesBetweenPhases(
          random, now, dependencies, deployTasks, testTasks, 0.9); // 90% deploy depends on test
    }

    taskDependencyRepository.saveAll(dependencies);
    return dependencies;
  }

  private void createDependenciesBetweenPhases(
      Random random,
      LocalDateTime now,
      List<TaskDependency> dependencies,
      List<Task> dependents,
      List<Task> dependees,
      double probability) {
    if (dependees.isEmpty() || dependents.isEmpty()) return;

    for (Task dependent : dependents) {
      if (random.nextDouble() <= probability) {
        // Pick a random dependee
        Task dependee = dependees.get(random.nextInt(dependees.size()));

        // Avoid cycle and duplicate
        if (!dependent.getId().equals(dependee.getId())
            && !taskDependencyRepository.existsByTaskIdAndDependsOnTaskId(
                dependent.getId(), dependee.getId())) {

          TaskDependency dep =
              TaskDependency.builder()
                  .task(dependent)
                  .dependsOnTask(dependee)
                  .createdBy(dependent.getCreatedBy())
                  .createdAt(now)
                  .updatedAt(now)
                  .build();
          dependencies.add(dep);
        }
      }
    }
  }

  private record ProjectPlan(
      int workspaceIndex,
      String name,
      ProjectStatusType status,
      String description,
      int offsetDays,
      int managerTeamIndex) {}

  private record TaskTypeTemplate(String name, String description, String color, String icon) {}

  private record UserSeedResult(List<User> allUsers, int createdUsers) {}

  private record MembershipSeedResult(
      List<WorkspaceMember> memberships,
      Map<Long, List<User>> membersByWorkspaceId,
      Map<Long, List<User>> adminsByWorkspaceId) {}

  private record TeamSeedResult(
      List<WorkspaceTeam> teams,
      List<WorkspaceTeamMember> teamMembers,
      Map<Long, List<WorkspaceTeam>> teamsByWorkspaceId,
      Map<Long, List<User>> membersByTeamId) {}

  private record TaskSeedResult(List<Task> tasks, Map<Long, List<Task>> tasksByProjectId) {}

  private record ReservedSlot(LocalDateTime start, LocalDateTime end) {}

  private record ScheduleSeedResult(
      List<TaskSchedule> schedules, Map<Long, List<TaskSchedule>> schedulesByTaskId) {}

  private record CommentSeedResult(
      List<TaskComment> comments, Map<Long, List<TaskComment>> commentsByTaskId) {}
}
