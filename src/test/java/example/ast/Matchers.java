package example.ast;

import rocks.matchmaker.Pattern;
import rocks.matchmaker.Property;

import static rocks.matchmaker.Pattern.typeOf;
import static rocks.matchmaker.Property.property;

public class Matchers {

    public static Pattern<JoinNode> join() {
        return typeOf(JoinNode.class);
    }

    public static Property<JoinNode, PlanNode> build() {
        return property(JoinNode::getBuild);
    }

    public static Property<JoinNode, PlanNode> probe() {
        return property(JoinNode::getProbe);
    }

    public static Pattern<ScanNode> scan() {
        return typeOf(ScanNode.class);
    }

    public static Pattern<FilterNode> filter() {
        return typeOf(FilterNode.class);
    }

    public static Pattern<PlanNode> plan() {
        return typeOf(PlanNode.class);
    }

    public static Pattern<ProjectNode> project() {
        return typeOf(ProjectNode.class);
    }

    public static Property<ScanNode, String> tableName() {
        return property(ScanNode::getTableName);
    }

    public static Property<SingleSourcePlanNode, PlanNode> source() {
        return property(SingleSourcePlanNode::getSource);
    }
}
