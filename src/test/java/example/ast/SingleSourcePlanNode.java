package example.ast;

import java.util.List;

import static java.util.Collections.singletonList;

public interface SingleSourcePlanNode extends PlanNode {

    PlanNode getSource();

    default List<PlanNode> getSources() {
        return singletonList(getSource());
    }
}
