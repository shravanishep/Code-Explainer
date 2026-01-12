import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class Main {

    public static void main(String[] args) {

        String code = """
            public class Test {
                public static void main(String[] args) {
                    for(int i = 0; i < 5; i++) {
                        System.out.println(i);
                    }
                }
            }
        """;

        try {
            CompilationUnit cu = StaticJavaParser.parse(code);
            System.out.println("Java code parsed successfully.");
        } catch (Exception e) {
            System.out.println("Invalid Java code.");
        }
    }
}
