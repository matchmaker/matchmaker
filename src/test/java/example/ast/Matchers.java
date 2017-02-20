package example.ast;

import rocks.matchmaker.Matcher;
import rocks.matchmaker.Property;

import static rocks.matchmaker.Matcher.typeOf;
import static rocks.matchmaker.Property.property;

public class Matchers {

    public static Matcher<JoinNode> join() {
        return typeOf(JoinNode.class);
    }

    public static Property<JoinNode, PlanNode> build() {
        return property(JoinNode::getBuild);
    }

    public static Property<JoinNode, PlanNode> probe() {
        return property(JoinNode::getProbe);
    }

    public static Matcher<ScanNode> scan() {
        return typeOf(ScanNode.class);
    }

    public static Matcher<FilterNode> filter() {
        return typeOf(FilterNode.class);
    }

    public static Matcher<PlanNode> plan() {
        return typeOf(PlanNode.class);
    }

    public static Matcher<ProjectNode> project() {
        return typeOf(ProjectNode.class);
    }

    public static Property<ScanNode, String> tableName() {
        return property(ScanNode::getTableName);
    }

    public static Property<SingleSourcePlanNode, PlanNode> source() {
        return property(SingleSourcePlanNode::getSource);
    }
}
