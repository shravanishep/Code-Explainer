import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.*;

enum LoopGrowth {
    CONSTANT, LINEAR, LOGARITHMIC, UNKNOWN
}

class MethodReport {
    String name;
    List<LoopGrowth> loops = new ArrayList<>();
    boolean isRecursive = false;
    Set<String> callsTo = new HashSet<>();
}

public class ComplexityVisitor extends VoidVisitorAdapter<Void> {

    private String currentMethod = null;
    private final Map<String, MethodReport> reports = new HashMap<>();

    public Collection<MethodReport> getReports() {
        detectMutualRecursion();
        return reports.values();
    }

    private void detectMutualRecursion() {
        // Check for cycles in the call graph
        for (String method : reports.keySet()) {
            if (hasCycle(method, new HashSet<>(), new HashSet<>())) {
                reports.get(method).isRecursive = true;
            }
        }
    }

    private boolean hasCycle(String method, Set<String> visiting, Set<String> visited) {
        if (visiting.contains(method)) return true;
        if (visited.contains(method)) return false;

        visiting.add(method);
        MethodReport report = reports.get(method);
        if (report != null) {
            for (String callee : report.callsTo) {
                if (hasCycle(callee, visiting, visited)) {
                    return true;
                }
            }
        }
        visiting.remove(method);
        visited.add(method);
        return false;
    }

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        currentMethod = n.getNameAsString();
        reports.putIfAbsent(currentMethod, new MethodReport());
        reports.get(currentMethod).name = currentMethod;

        super.visit(n, arg);
        currentMethod = null;
    }

    @Override
    public void visit(MethodCallExpr n, Void arg) {
        if (currentMethod != null) {
            String calledMethod = n.getNameAsString();
            reports.get(currentMethod).callsTo.add(calledMethod);
            
            if (calledMethod.equals(currentMethod)) {
                reports.get(currentMethod).isRecursive = true;
            }
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(ForStmt n, Void arg) {
        reports.get(currentMethod).loops.add(detectForLoopGrowth(n));
        super.visit(n, arg);
    }

    @Override
    public void visit(ForEachStmt n, Void arg) {
        // For-each loops are always linear in the size of the collection
        reports.get(currentMethod).loops.add(LoopGrowth.LINEAR);
        super.visit(n, arg);
    }

    @Override
    public void visit(WhileStmt n, Void arg) {
        reports.get(currentMethod).loops.add(detectWhileLoopGrowth(n));
        super.visit(n, arg);
    }

    @Override
    public void visit(DoStmt n, Void arg) {
        reports.get(currentMethod).loops.add(detectDoWhileGrowth(n));
        super.visit(n, arg);
    }

    private LoopGrowth detectForLoopGrowth(ForStmt n) {
        if (n.getUpdate().isEmpty()) return LoopGrowth.LINEAR;

        Expression update = n.getUpdate().get(0);

        // Handle i++, ++i, i--, --i
        if (update instanceof UnaryExpr) {
            UnaryExpr u = (UnaryExpr) update;
            if (u.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT ||
                u.getOperator() == UnaryExpr.Operator.PREFIX_INCREMENT ||
                u.getOperator() == UnaryExpr.Operator.POSTFIX_DECREMENT ||
                u.getOperator() == UnaryExpr.Operator.PREFIX_DECREMENT) {
                return LoopGrowth.LINEAR;
            }
        }

        // Handle i += k, i -= k, i *= k, i /= k
        if (update instanceof AssignExpr) {
            AssignExpr a = (AssignExpr) update;
            if (a.getOperator() == AssignExpr.Operator.MULTIPLY ||
                a.getOperator() == AssignExpr.Operator.DIVIDE) {
                return LoopGrowth.LOGARITHMIC;
            }
            if (a.getOperator() == AssignExpr.Operator.PLUS ||
                a.getOperator() == AssignExpr.Operator.MINUS) {
                return LoopGrowth.LINEAR;
            }
        }

        return LoopGrowth.LINEAR; // Default to linear if pattern unclear
    }

    private LoopGrowth detectWhileLoopGrowth(WhileStmt n) {
        // Analyze the body to detect update patterns
        Statement body = n.getBody();
        return analyzeLoopBody(body);
    }

    private LoopGrowth detectDoWhileGrowth(DoStmt n) {
        // Analyze the body to detect update patterns
        Statement body = n.getBody();
        return analyzeLoopBody(body);
    }

    private LoopGrowth analyzeLoopBody(Statement body) {
        // Look for common patterns in the loop body
        LoopBodyAnalyzer analyzer = new LoopBodyAnalyzer();
        body.accept(analyzer, null);
        
        if (analyzer.hasMultiplyOrDivide) {
            return LoopGrowth.LOGARITHMIC;
        }
        if (analyzer.hasIncrementOrAdd) {
            return LoopGrowth.LINEAR;
        }
        
        // Default to linear for while/do-while loops
        return LoopGrowth.LINEAR;
    }

    // Helper class to analyze loop body statements
    private static class LoopBodyAnalyzer extends VoidVisitorAdapter<Void> {
        boolean hasIncrementOrAdd = false;
        boolean hasMultiplyOrDivide = false;

        @Override
        public void visit(UnaryExpr n, Void arg) {
            if (n.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT ||
                n.getOperator() == UnaryExpr.Operator.PREFIX_INCREMENT ||
                n.getOperator() == UnaryExpr.Operator.POSTFIX_DECREMENT ||
                n.getOperator() == UnaryExpr.Operator.PREFIX_DECREMENT) {
                hasIncrementOrAdd = true;
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(AssignExpr n, Void arg) {
            if (n.getOperator() == AssignExpr.Operator.MULTIPLY ||
                n.getOperator() == AssignExpr.Operator.DIVIDE) {
                hasMultiplyOrDivide = true;
            }
            if (n.getOperator() == AssignExpr.Operator.PLUS ||
                n.getOperator() == AssignExpr.Operator.MINUS) {
                hasIncrementOrAdd = true;
            }
            super.visit(n, arg);
        }
    }
}
