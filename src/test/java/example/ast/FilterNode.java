package example.ast;

public class FilterNode implements PlanNode {

    private Expression predicate;

    public FilterNode(Expression predicate) {
        this.predicate = predicate;
    }

    public Expression getPredicate() {
        return predicate;
    }
}
