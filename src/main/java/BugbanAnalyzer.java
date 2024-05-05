import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.util.*;

public class BugbanAnalyzer {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        System.out.println("Enter the path for the first Bugban analysis output file:");
        String pathFirstFile = scanner.nextLine();

        System.out.println("Enter the path for the second Bugban analysis output file:");
        String pathSecondFile = scanner.nextLine();

        System.out.println("Enter the output file path for problems detected only in the first analysis:");
        String pathUniqueFirst = scanner.nextLine();

        System.out.println("Enter the output file path for problems detected only in the second analysis:");
        String pathUniqueSecond = scanner.nextLine();

        System.out.println("Enter the output file path for problems detected in both analyses:");
        String pathCommon = scanner.nextLine();

        try {
            ObjectMapper mapper = new ObjectMapper();

            BugbanReport firstReport = mapper.readValue(new File(pathFirstFile), BugbanReport.class);
            BugbanReport secondReport = mapper.readValue(new File(pathSecondFile), BugbanReport.class);

            Set<Problem> firstProblems = new HashSet<>(firstReport.getProblems());
            Set<Problem> secondProblems = new HashSet<>(secondReport.getProblems());

            Set<Problem> uniqueFirst = new HashSet<>(firstProblems);
            uniqueFirst.removeAll(secondProblems);

            Set<Problem> uniqueSecond = new HashSet<>(secondProblems);
            uniqueSecond.removeAll(firstProblems);

            Set<Problem> common = new HashSet<>(firstProblems);
            common.retainAll(secondProblems);

            writeJsonFile(mapper, uniqueFirst, pathUniqueFirst);
            writeJsonFile(mapper, uniqueSecond, pathUniqueSecond);
            writeJsonFile(mapper, common, pathCommon);

            System.out.println("Analysis complete. Files have been written.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred during processing: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void writeJsonFile(ObjectMapper mapper, Set<Problem> problems, String filePath) throws IOException {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode problemsNode = root.putArray("problems");
        problems.forEach(problem -> {
            ObjectNode jsonNode = mapper.createObjectNode();
            jsonNode.put("hash", problem.getHash());
            ArrayNode dataArray = jsonNode.putArray("data");
            problem.getData().forEach(dataArray::add);
            problemsNode.add(jsonNode);
        });
        mapper.writeValue(new File(filePath), root);
        System.out.println("Output written to " + filePath);
    }

    static class BugbanReport {
        private List<Problem> problems;

        public List<Problem> getProblems() {
            return problems;
        }

        public void setProblems(List<Problem> problems) {
            this.problems = problems;
        }
    }

    public static class Problem {
        private String hash;
        private Set<String> data;
        
        public Problem() {
        }

        @JsonCreator
        public Problem(@JsonProperty("hash") String hash, @JsonProperty("data") Set<String> data) {
            this.hash = hash;
            this.data = data != null ? new HashSet<>(data) : new HashSet<>();
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public Set<String> getData() {
            return data;
        }

        public void setData(Set<String> data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Problem problem = (Problem) o;
            return Objects.equals(hash, problem.hash) &&
                    Objects.equals(data, problem.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hash, data);
        }
    }
}