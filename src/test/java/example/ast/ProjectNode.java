package example.ast;

public class ProjectNode implements PlanNode {

    private PlanNode source;

    public ProjectNode(PlanNode source) {
        this.source = source;
    }

    public PlanNode getSource() {
        return source;
    }
}
