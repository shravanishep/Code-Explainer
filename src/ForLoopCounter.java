import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ForLoopCounter extends VoidVisitorAdapter<Void> {

    public int currentDepth = 0;
    public int maxDepth = 0;

    @Override
    public void visit(ForStmt n, Void arg) {
        currentDepth++;

        if (currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }

        super.visit(n, arg);

        currentDepth--;
    }
}
