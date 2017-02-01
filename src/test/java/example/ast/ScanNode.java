package example.ast;

import java.util.List;

import static java.util.Collections.emptyList;

public class ScanNode implements PlanNode {

    @Override
    public List<PlanNode> getSources() {
        return emptyList();
    }
}
