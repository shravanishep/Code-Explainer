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

                static void demo(int n) {
                    for(int i=0;i<n;i++) {
                        for(int j=1;j<n;j*=2) {
                            System.out.println(i + j);
                        }
                    }
                }

                public static void main(String[] args) {
                    System.out.println(fact(5));
                    demo(10);
                }
            }
        """;

        try {
            CompilationUnit cu = StaticJavaParser.parse(code);

            ComplexityVisitor visitor = new ComplexityVisitor();
            visitor.visit(cu, null);

            for (MethodReport r : visitor.getReports()) {
                printReport(r);
            }

            printLimitations();

        } catch (Exception e) {
            System.out.println("Invalid Java code");
        }
    }

    private static void printReport(MethodReport r) {
        System.out.println("================================");
        System.out.println("Method: " + r.name);

        String time = estimateTime(r);
        String space = r.isRecursive ? "O(n)" : "O(1)";

        System.out.println("Time Complexity: " + time);
        System.out.println("Space Complexity: " + space);

        if (r.isRecursive) {
            System.out.println("Reason: Recursive calls detected.");
        }

        if (!r.loops.isEmpty()) {
            System.out.println("Loop Nesting Depth: " + r.maxNestedLoopDepth);
            System.out.println("Loop Growth Types: " + r.loops);
        }

        System.out.println("================================\n");
    }

    private static String estimateTime(MethodReport r) {
        if (r.loops.isEmpty() && !r.isRecursive) return "O(1)";
        if (r.loops.isEmpty()) return "O(n)";

        // Only multiply loops up to the max nesting depth
        StringBuilder sb = new StringBuilder("O(");
        boolean first = true;
        
        int nestedCount = 0;
        for (LoopGrowth g : r.loops) {
            nestedCount++;
            if (nestedCount > r.maxNestedLoopDepth) {
                // Sequential loop - add instead of multiply
                sb.append(" + ");
            } else if (!first) {
                // Nested loop - multiply
                sb.append(" * ");
            }
            first = false;

            switch (g) {
                case CONSTANT -> sb.append("1");
                case LINEAR -> sb.append("n");
                case LOGARITHMIC -> sb.append("log n");
                default -> sb.append("?");
            }
        }

        sb.append(")");
        return sb.toString();
    }

    private static void printLimitations() {
        System.out.println("Analysis Features:");
        System.out.println("✓ Detects direct and mutual recursion");
        System.out.println("✓ Analyzes all loop types (for, foreach, while, do-while)");
        System.out.println("✓ Identifies linear, logarithmic, and constant growth patterns");
        System.out.println("✓ Provides worst-case complexity estimates");
        System.out.println("\nNote: Uses static analysis based on code structure");
    }
}
