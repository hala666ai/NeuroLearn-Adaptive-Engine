import java.util.*;

/**
 * AdaptiveLearningEngine v4.0
 * ---------------------------
 * A highly structured, scalable Java engine for:
 * - Adaptive exercise generation
 * - Reinforcement Learning (Q-Learning inspired)
 * - Difficulty scaling
 * - Weakness detection
 * - Performance tracking over time
 * - Smart study recommendations
 *
 * This is a console demo, but the architecture is designed for:
 * - Android apps
 * - Web apps (Spring Boot)
 * - LMS integration
 */

class AdaptiveLearningEngine {

    // -----------------------------
    // ENUMS & DATA MODELS
    // -----------------------------

    enum Topic {
        ADDITION,
        SUBTRACTION,
        MULTIPLICATION,
        DIVISION
    }

    static class Exercise {
        Topic topic;
        int difficulty;
        String question;
        int correctAnswer;

        Exercise(Topic topic, int difficulty, String question, int correctAnswer) {
            this.topic = topic;
            this.difficulty = difficulty;
            this.question = question;
            this.correctAnswer = correctAnswer;
        }
    }

    static class PerformanceRecord {
        long timestamp;
        boolean correct;

        PerformanceRecord(long timestamp, boolean correct) {
            this.timestamp = timestamp;
            this.correct = correct;
        }
    }

    static class TopicStats {
        List<PerformanceRecord> history = new ArrayList<>();

        void record(boolean correct) {
            history.add(new PerformanceRecord(System.currentTimeMillis(), correct));
        }

        double accuracy() {
            if (history.isEmpty()) return 0;
            int correct = 0;
            for (var r : history) if (r.correct) correct++;
            return (double) correct / history.size();
        }

        double recentAccuracy() {
            if (history.size() < 5) return accuracy();
            int correct = 0;
            for (int i = history.size() - 5; i < history.size(); i++)
                if (history.get(i).correct) correct++;
            return correct / 5.0;
        }
    }

    static class StudentProfile {
        String name;

        Map<Topic, Integer> difficulty = new EnumMap<>(Topic.class);
        Map<Topic, TopicStats> stats = new EnumMap<>(Topic.class);

        // Q-values for RL
        Map<Topic, Double> qValues = new EnumMap<>(Topic.class);

        StudentProfile(String name) {
            this.name = name;
            for (Topic t : Topic.values()) {
                difficulty.put(t, 3);
                stats.put(t, new TopicStats());
                qValues.put(t, 0.0);
            }
        }

        Topic weakestTopic() {
            Topic weakest = null;
            double minAcc = Double.MAX_VALUE;
            for (Topic t : Topic.values()) {
                double acc = stats.get(t).accuracy();
                if (acc < minAcc) {
                    minAcc = acc;
                    weakest = t;
                }
            }
            return weakest;
        }

        Topic strongestTopic() {
            Topic strongest = null;
            double maxAcc = -1;
            for (Topic t : Topic.values()) {
                double acc = stats.get(t).accuracy();
                if (acc > maxAcc) {
                    maxAcc = acc;
                    strongest = t;
                }
            }
            return strongest;
        }
    }

    // -----------------------------
    // EXERCISE GENERATOR
    // -----------------------------

    static class ExerciseGenerator {
        Random r = new Random();

        Exercise generate(Topic topic, int difficulty) {
            int max = difficulty * 10 + 5;
            int a = r.nextInt(max) + 1;
            int b = r.nextInt(max) + 1;

            switch (topic) {
                case ADDITION:
                    return new Exercise(topic, difficulty, a + " + " + b + " = ?", a + b);
                case SUBTRACTION:
                    if (b > a) { int t = a; a = b; b = t; }
                    return new Exercise(topic, difficulty, a + " - " + b + " = ?", a - b);
                case MULTIPLICATION:
                    return new Exercise(topic, difficulty, a + " × " + b + " = ?", a * b);
                case DIVISION:
                    int product = a * b;
                    return new Exercise(topic, difficulty, product + " ÷ " + a + " = ?", b);
            }
            return null;
        }
    }

    // -----------------------------
    // RL POLICY (Q-Learning Inspired)
    // -----------------------------

    static class RLPolicy {
        double alpha = 0.3;   // learning rate
        double gamma = 0.8;   // discount factor
        double rewardCorrect = 1.0;
        double rewardWrong = -1.0;

        Topic chooseTopic(StudentProfile s) {
            Random r = new Random();
            if (r.nextDouble() < 0.6)
                return s.weakestTopic();

            Topic[] topics = Topic.values();
            return topics[r.nextInt(topics.length)];
        }

        void update(StudentProfile s, Topic t, boolean correct) {
            double reward = correct ? rewardCorrect : rewardWrong;
            double oldQ = s.qValues.get(t);
            double newQ = oldQ + alpha * (reward + gamma * oldQ - oldQ);
            s.qValues.put(t, newQ);

            int diff = s.difficulty.get(t);
            double acc = s.stats.get(t).recentAccuracy();

            if (acc > 0.8 && newQ > 0)
                s.difficulty.put(t, Math.min(10, diff + 1));
            else if (acc < 0.5 && newQ < 0)
                s.difficulty.put(t, Math.max(1, diff - 1));
        }
    }

    // -----------------------------
    // SMART STUDY PLAN
    // -----------------------------

    static class StudyPlan {
        String message;

        StudyPlan(String message) {
            this.message = message;
        }
    }

    static StudyPlan buildPlan(StudentProfile s) {
        Topic weak = s.weakestTopic();
        Topic strong = s.strongestTopic();

        StringBuilder sb = new StringBuilder();
        sb.append("=== Smart Study Plan ===\n");
        sb.append("Weakest topic: ").append(weak).append("\n");
        sb.append("Strongest topic: ").append(strong).append("\n\n");

        double acc = s.stats.get(weak).accuracy();

        if (acc < 0.4)
            sb.append("Start with easy exercises in ").append(weak).append(" and build confidence.\n");
        else if (acc < 0.7)
            sb.append("Practice medium difficulty in ").append(weak).append(" and review mistakes.\n");
        else
            sb.append("You are close to mastery in ").append(weak).append(". Try harder problems.\n");

        sb.append("\nTip: Mix strong and weak topics to balance confidence and growth.");

        return new StudyPlan(sb.toString());
    }
}

// -----------------------------
// MAIN PROGRAM
// -----------------------------

public class Main {
    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);
        AdaptiveLearningEngine.ExerciseGenerator gen = new AdaptiveLearningEngine.ExerciseGenerator();
        AdaptiveLearningEngine.RLPolicy policy = new AdaptiveLearningEngine.RLPolicy();

        System.out.print("Enter student name: ");
        String name = in.nextLine().trim();
        if (name.isEmpty()) name = "Student";

        AdaptiveLearningEngine.StudentProfile s = new AdaptiveLearningEngine.StudentProfile(name);

        System.out.println("\n=== Adaptive Learning Tutor v4.0 ===");
        System.out.println("Commands: plan | exit\n");

        while (true) {
            System.out.print("\nPress ENTER for next exercise: ");
            String cmd = in.nextLine().trim().toLowerCase();

            if (cmd.equals("exit")) break;
            if (cmd.equals("plan")) {
                System.out.println(AdaptiveLearningEngine.buildPlan(s).message);
                continue;
            }

            AdaptiveLearningEngine.Topic topic = policy.chooseTopic(s);
            int diff = s.difficulty.get(topic);

            AdaptiveLearningEngine.Exercise ex = gen.generate(topic, diff);

            System.out.println("\nTopic: " + topic);
            System.out.println("Difficulty: " + diff);
            System.out.println("Question: " + ex.question);
            System.out.print("Your answer: ");

            String ans = in.nextLine().trim();
            boolean correct = false;

            try {
                correct = (Integer.parseInt(ans) == ex.correctAnswer);
            } catch (Exception e) {
                correct = false;
            }

            s.stats.get(topic).record(correct);
            policy.update(s, topic, correct);

            if (correct)
                System.out.println("✔ Correct!");
            else
                System.out.println("✘ Wrong. Correct answer: " + ex.correctAnswer);
        }

        System.out.println("\nGoodbye " + s.name + "!");
    }
}