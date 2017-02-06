package example.ast;

import java.util.List;

import static java.util.Arrays.asList;

public class Exchange implements PlanNode{

    private List<PlanNode> sources;

    public Exchange(PlanNode... sources) {
        this.sources = asList(sources);
    }

    @Override
    public List<PlanNode> getSources() {
        return sources;
    }
}
