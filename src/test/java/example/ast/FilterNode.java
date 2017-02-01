package example.ast;

public class FilterNode implements SingleSourcePlanNode {

    private PlanNode source;
    private Expression predicate;

    public FilterNode(PlanNode source, Expression predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public PlanNode getSource() {
        return source;
    }

    public Expression getPredicate() {
        return predicate;
    }
}
