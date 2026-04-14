package com.careerassistant.service.impl;

import com.careerassistant.dto.skillgap.SkillGapResponse;
import com.careerassistant.service.SkillGapAnalyzer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DynamicSkillGapAnalyzer implements SkillGapAnalyzer {

    private static final Map<String, Set<String>> ROLE_SKILLS_MAP = new HashMap<>();
    private static final Map<String, List<String>> SKILL_LEARNING_ORDER = new LinkedHashMap<>();

    static {
        initializeRoleSkills();
        initializeLearningOrder();
    }

    private static void initializeRoleSkills() {
        // Core programming languages
        Set<String> javaDev = new HashSet<>(Arrays.asList(
                "Java", "Spring Boot", "Spring MVC", "Spring Cloud", "Hibernate", "JPA", "Maven", "Gradle",
                "REST API", "REST APIs", "Microservices", "SQL", "MySQL", "PostgreSQL",
                "Git", "Docker", "AWS", "EC2", "S3", "Lambda", "Kafka", "Redis", "MongoDB"
        ));
        ROLE_SKILLS_MAP.put("java developer", javaDev);
        ROLE_SKILLS_MAP.put("senior java developer", new HashSet<>(javaDev));
        ROLE_SKILLS_MAP.get("senior java developer").addAll(Arrays.asList(
                "Kubernetes", "Terraform", "Design Patterns", "System Design", "CI/CD", "Jenkins", "Leadership"
        ));

        Set<String> fullStack = new HashSet<>(Arrays.asList(
                "Java", "Spring Boot", "JavaScript", "React", "Angular", "Vue", "HTML", "CSS",
                "TypeScript", "REST APIs", "GraphQL", "SQL", "MongoDB", "PostgreSQL",
                "Docker", "Git", "CI/CD", "Node.js", "Express"
        ));
        ROLE_SKILLS_MAP.put("full stack developer", fullStack);
        ROLE_SKILLS_MAP.put("fullstack developer", fullStack);

        Set<String> frontend = new HashSet<>(Arrays.asList(
                "React", "JavaScript", "TypeScript", "HTML", "CSS", "SASS", "Redux",
                "Context API", "Hooks", "Next.js", "Vue", "Angular", "Webpack", "Vite",
                "Responsive Design", "REST APIs", "Git", "Testing", "Jest"
        ));
        ROLE_SKILLS_MAP.put("frontend developer", frontend);
        ROLE_SKILLS_MAP.put("front end developer", frontend);
        ROLE_SKILLS_MAP.put("react developer", frontend);
        ROLE_SKILLS_MAP.put("reactjs developer", frontend);

        Set<String> backend = new HashSet<>(Arrays.asList(
                "Java", "Python", "Node.js", "Spring Boot", "Django", "Flask", "Express",
                "REST APIs", "GraphQL", "SQL", "PostgreSQL", "MongoDB", "Redis",
                "Docker", "AWS", "Git", "Microservices"
        ));
        ROLE_SKILLS_MAP.put("backend developer", backend);
        ROLE_SKILLS_MAP.put("back end developer", backend);

        Set<String> pythonDev = new HashSet<>(Arrays.asList(
                "Python", "Django", "Flask", "FastAPI", "SQL", "PostgreSQL", "MongoDB",
                "REST APIs", "Docker", "AWS", "Git", "Pandas", "NumPy", "Machine Learning"
        ));
        ROLE_SKILLS_MAP.put("python developer", pythonDev);
        ROLE_SKILLS_MAP.put("python developer", pythonDev);

        Set<String> devops = new HashSet<>(Arrays.asList(
                "Linux", "Docker", "Kubernetes", "AWS", "Azure", "GCP", "Terraform", "Ansible",
                "CI/CD", "Jenkins", "GitLab CI", "Prometheus", "Grafana", "Shell Scripting",
                "Networking", "Security", "Monitoring"
        ));
        ROLE_SKILLS_MAP.put("devops engineer", devops);
        ROLE_SKILLS_MAP.put("sre", devops);

        Set<String> awsDev = new HashSet<>(Arrays.asList(
                "AWS", "EC2", "S3", "Lambda", "DynamoDB", "RDS", "SQS", "SNS", "CloudFormation",
                "Terraform", "Docker", "Python", "Java", "Serverless", "IAM", "VPC", "EKS"
        ));
        ROLE_SKILLS_MAP.put("aws developer", awsDev);
        ROLE_SKILLS_MAP.put("aws engineer", awsDev);
        ROLE_SKILLS_MAP.put("cloud engineer", awsDev);

        Set<String> cppDev = new HashSet<>(Arrays.asList(
                "C++", "C", "STL", "Templates", "Pointers", "Memory Management", "Multithreading",
                "Linux", "Makefile", "CMake", "GDB", "Git", "Design Patterns", "OOP"
        ));
        ROLE_SKILLS_MAP.put("cpp developer", cppDev);
        ROLE_SKILLS_MAP.put("c++ developer", cppDev);
        ROLE_SKILLS_MAP.put("c developer", new HashSet<>(Arrays.asList("C", "C++", "Linux", "Memory Management", "Pointers", "STL", "Makefile", "Git")));

        Set<String> dataScience = new HashSet<>(Arrays.asList(
                "Python", "Machine Learning", "Deep Learning", "TensorFlow", "PyTorch", "Keras",
                "Pandas", "NumPy", "SQL", "Statistics", "Data Visualization", "NLP",
                "Computer Vision", "AWS", "Docker", "Scikit-learn"
        ));
        ROLE_SKILLS_MAP.put("data scientist", dataScience);
        ROLE_SKILLS_MAP.put("data analyst", new HashSet<>(Arrays.asList("Python", "SQL", "Pandas", "NumPy", "Tableau", "Power BI", "Excel", "Statistics")));

        // Default generic skills
        Set<String> generic = new HashSet<>(Arrays.asList(
                "Problem Solving", "OOP", "Data Structures", "Algorithms", "Git",
                "SQL", "REST APIs", "Testing", "Agile", "Communication"
        ));
        ROLE_SKILLS_MAP.put("software engineer", generic);
        ROLE_SKILLS_MAP.put("software developer", generic);
        ROLE_SKILLS_MAP.put("web developer", new HashSet<>(Arrays.asList("HTML", "CSS", "JavaScript", "React", "Node.js", "SQL", "Git", "Responsive Design")));
    }

    private static void initializeLearningOrder() {
        SKILL_LEARNING_ORDER.put("Java", Arrays.asList(
                "Java basics: syntax, OOP, Collections",
                "Java 8+: Streams, Lambdas, Optional",
                "Multithreading and Concurrency"
        ));
        SKILL_LEARNING_ORDER.put("Spring Boot", Arrays.asList(
                "Spring IoC, DI, and Spring Boot setup",
                "REST APIs with Spring MVC",
                "Spring Data JPA and database integration"
        ));
        SKILL_LEARNING_ORDER.put("React", Arrays.asList(
                "React basics: Components, JSX, Props",
                "State management: Hooks, Context API",
                "Advanced: Redux, Next.js, Testing"
        ));
        SKILL_LEARNING_ORDER.put("Python", Arrays.asList(
                "Python basics: syntax, data types, OOP",
                "File handling, exceptions, libraries",
                "Web development: Flask/Django"
        ));
        SKILL_LEARNING_ORDER.put("Docker", Arrays.asList(
                "Docker basics: Images, Containers",
                "Dockerfile and Docker Compose",
                "Networking and volumes"
        ));
        SKILL_LEARNING_ORDER.put("AWS", Arrays.asList(
                "EC2, S3, IAM basics",
                "Lambda and Serverless",
                "RDS, DynamoDB, VPC networking"
        ));
        SKILL_LEARNING_ORDER.put("Kubernetes", Arrays.asList(
                "Pods, ReplicaSets, Deployments",
                "Services, Ingress networking",
                "ConfigMaps, Secrets, Storage"
        ));
        SKILL_LEARNING_ORDER.put("SQL", Arrays.asList(
                "SELECT, WHERE, JOIN operations",
                "Aggregations and subqueries",
                "Performance and indexes"
        ));
        SKILL_LEARNING_ORDER.put("C++", Arrays.asList(
                "C++ basics: syntax, OOP, STL",
                "Memory management, pointers, references",
                "Multithreading and modern C++ features"
        ));
    }

    @Override
    public List<String> extractSkillsFromResume(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            log.info("Resume text is empty, returning empty skills list");
            return Collections.emptyList();
        }

        String lowerText = resumeText.toLowerCase();
        Set<String> foundSkills = new LinkedHashSet<>();

        // Comprehensive skill patterns for regex matching
        String[][] skillPatterns = {
                // Languages
                {"\\bjava\\b(?!\\s*script)", "Java"},
                {"\\bjavascript\\b", "JavaScript"},
                {"\\bpython\\b", "Python"},
                {"\\bc\\+\\+\\b", "C++"},
                {"\\bc\\b(?!\\+\\+|\\#)", "C"},
                {"\\btypescript\\b", "TypeScript"},
                {"\\bgo\\b|golang", "Go"},
                {"\\brust\\b", "Rust"},
                {"\\bswift\\b", "Swift"},
                {"\\bkotlin\\b", "Kotlin"},
                {"\\bscala\\b", "Scala"},
                {"\\bruby\\b", "Ruby"},
                {"\\bphp\\b", "PHP"},
                {"\\b\\.net\\b|dot\\s*net", ".NET"},

                // Frameworks
                {"spring\\s*(boot|mvc|cloud)?", "Spring"},
                {"react\\s*(js|hooks|redux)?", "React"},
                {"angular\\s*(js)?", "Angular"},
                {"vue\\s*(js)?", "Vue"},
                {"node\\s*(js)?", "Node.js"},
                {"express\\s*(js)?", "Express"},
                {"django", "Django"},
                {"flask", "Flask"},
                {"fastapi", "FastAPI"},
                {"next\\s*(js)?", "Next.js"},
                {"flutter", "Flutter"},
                {"react\\s*native", "React Native"},

                // Databases
                {"mysql", "MySQL"},
                {"postgresql", "PostgreSQL"},
                {"mongodb", "MongoDB"},
                {"redis", "Redis"},
                {"elasticsearch", "Elasticsearch"},
                {"cassandra", "Cassandra"},
                {"dynamodb", "DynamoDB"},
                {"oracle", "Oracle"},
                {"sql\\s*server", "SQL Server"},

                // Cloud & DevOps
                {"aws", "AWS"},
                {"amazon\\s*web\\s*services", "AWS"},
                {"azure", "Azure"},
                {"gcp|google\\s*cloud", "GCP"},
                {"docker", "Docker"},
                {"kubernetes|k8s", "Kubernetes"},
                {"jenkins", "Jenkins"},
                {"terraform", "Terraform"},
                {"ansible", "Ansible"},
                {"ci/cd", "CI/CD"},
                {"gitlab", "GitLab"},
                {"github", "GitHub"},

                // Skills
                {"git", "Git"},
                {"rest\\s*(api)?", "REST APIs"},
                {"graphql", "GraphQL"},
                {"microservices", "Microservices"},
                {"oauth", "OAuth"},
                {"jwt", "JWT"},
                {"linux", "Linux"},
                {"unix", "Unix"},
                {"windows\\s*(server)?", "Windows"},
                {"bash", "Bash"},
                {"shell\\s*script", "Shell Scripting"},
                {"agile", "Agile"},
                {"scrum", "Scrum"},

                // Data & ML
                {"machine\\s*learning|ml", "Machine Learning"},
                {"deep\\s*learning", "Deep Learning"},
                {"tensorflow", "TensorFlow"},
                {"pytorch", "PyTorch"},
                {"pandas", "Pandas"},
                {"numpy", "NumPy"},
                {"data\\s*science", "Data Science"},
                {"nlp", "NLP"},
                {"computer\\s*vision", "Computer Vision"},

                // Testing
                {"junit", "JUnit"},
                {"selenium", "Selenium"},
                {"jest", "Jest"},
                {"testing\\s*library", "Testing Library"},
                {"mocha", "Mocha"},

                // Other
                {"html", "HTML"},
                {"css", "CSS"},
                {"sass|scss", "SASS"},
                {"webpack", "Webpack"},
                {"maven", "Maven"},
                {"gradle", "Gradle"},
                {"kafka", "Kafka"},
                {"rabbitmq", "RabbitMQ"},
                {"jira", "JIRA"},
                {"oop", "OOP"},
                {"design\\s*patterns", "Design Patterns"},
                {"system\\s*design", "System Design"},
                {"algorithms", "Algorithms"},
                {"data\\s*structures", "Data Structures"}
        };

        for (String[] pattern : skillPatterns) {
            Pattern p = Pattern.compile(pattern[0], Pattern.CASE_INSENSITIVE);
            if (p.matcher(lowerText).find()) {
                foundSkills.add(pattern[1]);
            }
        }

        log.info("Extracted {} skills from resume", foundSkills.size());
        return new ArrayList<>(foundSkills);
    }

    @Override
    public Set<String> getRequiredSkillsForRole(String role) {
        if (role == null || role.isBlank()) {
            return Collections.emptySet();
        }

        String lowerRole = role.toLowerCase().trim();
        log.info("Looking for skills for role: {}", lowerRole);

        // Exact match
        Set<String> exactMatch = ROLE_SKILLS_MAP.get(lowerRole);
        if (exactMatch != null && !exactMatch.isEmpty()) {
            log.info("Found exact match with {} skills", exactMatch.size());
            return new LinkedHashSet<>(exactMatch);
        }

        // Partial match
        for (Map.Entry<String, Set<String>> entry : ROLE_SKILLS_MAP.entrySet()) {
            String key = entry.getKey();
            if (lowerRole.contains(key) || key.contains(lowerRole)) {
                log.info("Found partial match: {}", key);
                return new LinkedHashSet<>(entry.getValue());
            }
        }

        // Infer from keywords
        Set<String> inferred = inferSkillsFromRoleName(lowerRole);
        log.info("Inferred {} skills for role", inferred.size());
        return inferred;
    }

    private Set<String> inferSkillsFromRoleName(String role) {
        Set<String> skills = new LinkedHashSet<>();

        if (role.contains("java")) skills.addAll(Arrays.asList("Java", "Spring Boot", "SQL", "Git"));
        if (role.contains("python")) skills.addAll(Arrays.asList("Python", "SQL", "Git"));
        if (role.contains("react") || role.contains("frontend")) skills.addAll(Arrays.asList("React", "JavaScript", "HTML", "CSS", "REST APIs"));
        if (role.contains("node")) skills.addAll(Arrays.asList("Node.js", "JavaScript", "REST APIs", "MongoDB"));
        if (role.contains("full stack") || role.contains("fullstack")) skills.addAll(Arrays.asList("React", "Node.js", "SQL", "MongoDB", "Docker", "Git"));
        if (role.contains("backend") || role.contains("api")) skills.addAll(Arrays.asList("Java", "Spring Boot", "REST APIs", "SQL", "Docker"));
        if (role.contains("devops") || role.contains("sre")) skills.addAll(Arrays.asList("Docker", "Kubernetes", "AWS", "Linux", "CI/CD", "Terraform"));
        if (role.contains("data") || role.contains("analytics")) skills.addAll(Arrays.asList("Python", "SQL", "Machine Learning", "Pandas"));
        if (role.contains("cloud")) skills.addAll(Arrays.asList("AWS", "Docker", "Kubernetes", "Terraform"));
        if (role.contains("mobile") && role.contains("android")) skills.addAll(Arrays.asList("Kotlin", "Java", "Android SDK", "REST APIs"));
        if (role.contains("mobile") && role.contains("ios")) skills.addAll(Arrays.asList("Swift", "iOS SDK", "REST APIs"));
        if (role.contains("ml") || role.contains("machine learning")) skills.addAll(Arrays.asList("Python", "Machine Learning", "TensorFlow", "SQL"));
        if (role.contains("security")) skills.addAll(Arrays.asList("Security", "OAuth", "JWT", "SSL"));
        if (role.contains("c++") || role.contains("cpp")) skills.addAll(Arrays.asList("C++", "STL", "Linux", "Design Patterns"));
        if (role.contains("embedded") || role.contains("firmware")) skills.addAll(Arrays.asList("C", "C++", "Embedded Systems", "RTOS"));

        if (skills.isEmpty()) {
            skills.addAll(Arrays.asList("Problem Solving", "OOP", "Data Structures", "Algorithms", "Git", "SQL", "REST APIs"));
        }

        return skills;
    }

    @Override
    public List<String> calculateMissingSkills(List<String> currentSkills, Set<String> requiredSkills) {
        if (currentSkills == null || currentSkills.isEmpty()) {
            return new ArrayList<>(requiredSkills);
        }

        Set<String> currentSet = currentSkills.stream()
                .map(String::toLowerCase)
                .map(s -> s.replace(" ", "").replace("-", "").replace("_", ""))
                .collect(Collectors.toSet());

        return requiredSkills.stream()
                .filter(required -> {
                    String normalized = required.toLowerCase().replace(" ", "").replace("-", "").replace("_", "");
                    return currentSet.stream().noneMatch(current ->
                            current.equals(normalized) ||
                            current.contains(normalized) ||
                            normalized.contains(current)
                    );
                })
                .sorted(Comparator.comparing(s -> getPriorityScore(s)))
                .collect(Collectors.toList());
    }

    private int getPriorityScore(String skill) {
        Map<String, Integer> priority = new HashMap<>();
        priority.put("java", 1);
        priority.put("python", 2);
        priority.put("javascript", 3);
        priority.put("typescript", 4);
        priority.put("spring boot", 5);
        priority.put("react", 6);
        priority.put("node.js", 7);
        priority.put("sql", 8);
        priority.put("mongodb", 9);
        priority.put("aws", 10);
        priority.put("docker", 11);
        priority.put("kubernetes", 12);
        priority.put("git", 13);
        priority.put("rest apis", 14);
        priority.put("microservices", 15);

        return priority.getOrDefault(skill.toLowerCase(), 100);
    }

    @Override
    public List<String> generateDynamicRoadmap(List<String> missingSkills, String targetRole) {
        if (missingSkills == null || missingSkills.isEmpty()) {
            return Arrays.asList(
                    "🎉 Great news! You have all the required skills for this role.",
                    "💡 Focus on advanced projects and interview preparation.",
                    "📚 Explore system design and behavioral questions."
            );
        }

        List<Map<String, String>> phases = new ArrayList<>();

        // Phase 1: Fundamentals
        List<String> fundamentals = missingSkills.stream()
                .filter(s -> SKILL_LEARNING_ORDER.containsKey(s))
                .limit(3)
                .collect(Collectors.toList());

        if (!fundamentals.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("📖 Phase 1 - Fundamentals:\n");
            for (String skill : fundamentals) {
                List<String> steps = SKILL_LEARNING_ORDER.get(skill);
                if (steps != null && !steps.isEmpty()) {
                    sb.append("• ").append(skill).append(": ").append(steps.get(0)).append("\n");
                }
            }
            phases.add(Map.of("title", "Phase 1 - Fundamentals", "content", sb.toString()));
        }

        // Phase 2: Core Skills
        List<String> coreSkills = missingSkills.stream()
                .filter(s -> !fundamentals.contains(s))
                .limit(4)
                .collect(Collectors.toList());

        if (!coreSkills.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("⚡ Phase 2 - Core Skills:\n");
            for (String skill : coreSkills) {
                List<String> steps = SKILL_LEARNING_ORDER.get(skill);
                if (steps != null && steps.size() > 1) {
                    sb.append("• ").append(skill).append(": ").append(steps.get(1)).append("\n");
                } else {
                    sb.append("• Master ").append(skill).append("\n");
                }
            }
            phases.add(Map.of("title", "Phase 2 - Core Skills", "content", sb.toString()));
        }

        // Phase 3: Projects
        if (!coreSkills.isEmpty()) {
            phases.add(Map.of(
                    "title", "Phase 3 - Project Work",
                    "content", "🔨 Build 1-2 projects integrating:\n• " + String.join("\n• ", coreSkills) + "\n• Add to GitHub portfolio"
            ));
        }

        // Phase 4: Interview Prep
        phases.add(Map.of(
                "title", "Phase 4 - Interview Prep",
                "content", "Practice:\n- Coding problems (LeetCode/HackerRank)\n- " + targetRole + " interview questions\n- STAR method for behavioral questions"
        ));

        // Convert to flat list
        List<String> roadmap = new ArrayList<>();
        for (Map<String, String> phase : phases) {
            roadmap.add("[PHASE]" + phase.get("title"));
            String[] lines = phase.get("content").split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    roadmap.add("[STEP]" + line.trim());
                }
            }
        }

        return roadmap;
    }

    @Override
    public SkillGapResponse analyzeSkillGap(String resumeText, String targetRole) {
        log.info("Analyzing skill gap for role: {}", targetRole);

        List<String> currentSkills = extractSkillsFromResume(resumeText);
        Set<String> requiredSkills = getRequiredSkillsForRole(targetRole);
        List<String> missingSkills = calculateMissingSkills(currentSkills, requiredSkills);
        List<String> roadmap = generateDynamicRoadmap(missingSkills, targetRole);

        log.info("Result: {} current, {} required, {} missing", currentSkills.size(), requiredSkills.size(), missingSkills.size());

        return new SkillGapResponse(targetRole, currentSkills, missingSkills, roadmap);
    }

    @Override
    public SkillGapResponse analyzeSkillGapWithSkills(String resumeText, String targetRole, List<String> providedSkills) {
        log.info("Analyzing skill gap with provided skills for role: {}", targetRole);

        Set<String> requiredSkills = getRequiredSkillsForRole(targetRole);
        List<String> missingSkills = calculateMissingSkills(providedSkills, requiredSkills);
        List<String> roadmap = generateDynamicRoadmap(missingSkills, targetRole);

        log.info("Result: {} provided, {} required, {} missing", providedSkills.size(), requiredSkills.size(), missingSkills.size());

        return new SkillGapResponse(targetRole, providedSkills, missingSkills, roadmap);
    }
}
