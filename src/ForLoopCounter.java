import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import java.util.HashSet;
import java.util.Set;

public class ForLoopCounter extends VoidVisitorAdapter<Void> {
private String currentMethod = null;
public Set<String> recursiveMethods = new HashSet<>();

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



    @Override
public void visit(MethodDeclaration n, Void arg) {
    String previousMethod = currentMethod;
    currentMethod = n.getNameAsString();

    super.visit(n, arg);

    currentMethod = previousMethod;
}


@Override
public void visit(MethodCallExpr n, Void arg) {
    if (currentMethod != null &&
        n.getNameAsString().equals(currentMethod)) {
        recursiveMethods.add(currentMethod);
    }
    super.visit(n, arg);
}


}
