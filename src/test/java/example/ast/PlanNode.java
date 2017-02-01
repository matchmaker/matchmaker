package example.ast;

import java.util.List;

public interface PlanNode {

    List<PlanNode> getSources();
}