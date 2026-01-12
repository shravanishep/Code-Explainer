import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ForLoopCounter extends VoidVisitorAdapter<Void> {

    public int currentDepth = 0;
    public int maxDepth = 0;

    private void enterLoop() {
        currentDepth++;
        if (currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }
    }

    private void exitLoop() {
        currentDepth--;
    }

    @Override
    public void visit(ForStmt n, Void arg) {
        enterLoop();
        super.visit(n, arg);
        exitLoop();
    }

    @Override
    public void visit(WhileStmt n, Void arg) {
        enterLoop();
        super.visit(n, arg);
        exitLoop();
    }

    @Override
    public void visit(DoStmt n, Void arg) {
        enterLoop();
        super.visit(n, arg);
        exitLoop();
    }
}
