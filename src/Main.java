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



        } catch (Exception e) {
            System.out.println("Invalid Java code");
        }
    }
}
