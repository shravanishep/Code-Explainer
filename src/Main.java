import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class Main {

    public static void main(String[] args) {

        String code = """
            public class Test {

                    static int fact(int n) {
            if (n == 0) return 1;
            return n * fact(n - 1);
        }
                public static void main(String[] args) {
               

         System.out.println(fact(5));


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
            System.out.println("Recursive methods: " + counter.recursiveMethods);
            String spaceComplexity;

if (counter.recursiveMethods.isEmpty()) {
    spaceComplexity = "O(1)";
} else {
    spaceComplexity = "O(n)";
}

System.out.println("Estimated Space Complexity: " + spaceComplexity);

if (depth == 0) {
    System.out.println("Time Complexity Reason: No loops detected, constant time execution.");
} else if (depth == 1) {
    System.out.println("Time Complexity Reason: Single loop detected, linear growth with input size.");
} else {
    System.out.println(
        "Time Complexity Reason: " + depth +
        " nested loops detected. Each inner loop executes fully for every iteration of the outer loop."
    );
}


if (counter.recursiveMethods.isEmpty()) {
    System.out.println("Space Complexity Reason: No recursion detected, constant stack space used.");
} else {
    System.out.println(
        "Space Complexity Reason: Recursive method(s) detected " +
        counter.recursiveMethods +
        ". Each recursive call adds a stack frame."
    );
}


        } catch (Exception e) {
            System.out.println("Invalid Java code");
        }
    }
}
