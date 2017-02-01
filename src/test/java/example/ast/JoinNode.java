package example.ast;

import java.util.List;

import static java.util.Arrays.asList;

public class JoinNode implements PlanNode {

    private PlanNode probe;
    private PlanNode build;

    public JoinNode(PlanNode probe, PlanNode build) {
        this.probe = probe;
        this.build = build;
    }

    @Override
    public List<PlanNode> getSources() {
        return asList(probe, build);
    }
}
