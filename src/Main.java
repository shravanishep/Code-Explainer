import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class Main {

    public static void main(String[] args) {

        String code = """
            public class Test {
                public static void main(String[] args) {
                    while (true) {
    for (int i = 0; i < n; i++) {
        do {
            x++;
        } while (x < n);
    }
}

                }
            }
        """;

        try {
            CompilationUnit cu = StaticJavaParser.parse(code);

            ForLoopCounter counter = new ForLoopCounter();
            counter.visit(cu, null);

            int depth = counter.maxDepth;

            String timeComplexity;
            if (depth == 0) {
                timeComplexity = "O(1)";
            } else if (depth == 1) {
                timeComplexity = "O(n)";
            } else {
                timeComplexity = "O(n^" + depth + ")";
            }

            System.out.println("Max for-loop nesting depth: " + depth);
            System.out.println("Estimated Time Complexity: " + timeComplexity);

        } catch (Exception e) {
            System.out.println("Invalid Java code");
        }
    }
}
