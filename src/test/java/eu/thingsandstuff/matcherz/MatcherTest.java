package eu.thingsandstuff.matcherz;

import example.ast.Expression;
import example.ast.FilterNode;
import example.ast.PlanNode;
import example.ast.ProjectNode;
import org.junit.jupiter.api.Test;

import static eu.thingsandstuff.matcherz.Matcher.any;
import static eu.thingsandstuff.matcherz.Property.$;

@SuppressWarnings("WeakerAccess")
public class MatcherTest {

    Property<ProjectNode, PlanNode> source = $(ProjectNode::getSource);

    Capture<PlanNode> filter = Capture.make();
    Capture<ProjectNode> parent = Capture.make();
    Matcher<Expression> disjunction = Matcher.of(Expression.class);
    Matcher<ProjectNode> Project = Matcher.of(ProjectNode.class);
    Matcher<FilterNode> Filter = Matcher.of(FilterNode.class);

//    Matcher<ProjectNode> matcher = Project.as(parent)
//            .with(source.matching(Filter.as(filter)));
//                    .with($(FilterNode::getPredicate).matching(disjunction))));

    @Test
    public void match_object() {
        assertMatch(Project, new ProjectNode(null));
        assertNoMatch(Project, new FilterNode(null));
    }


    @Test
    public void match_property() {
        Matcher<example.ast.ProjectNode> matcher = Project
                .with($(ProjectNode::getSource).matching(any()));
        assertMatch(matcher, new ProjectNode(null));
        assertNoMatch(matcher, new FilterNode(null));
    }

    private void assertMatch(Matcher<?> matcher, Object expectedMatch) {

    }

    private void assertNoMatch(Matcher<?> matcher, Object expectedNoMatch) {

    }
}