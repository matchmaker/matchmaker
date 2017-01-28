package eu.thingsandstuff.matcherz;

import example.ast.Expression;
import example.ast.FilterNode;
import example.ast.PlanNode;
import example.ast.ProjectNode;
import org.junit.jupiter.api.Test;

import static eu.thingsandstuff.matcherz.Matcher.any;
import static eu.thingsandstuff.matcherz.Matcher.match;
import static eu.thingsandstuff.matcherz.Property.$;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("WeakerAccess")
public class MatcherTest {

    Property<ProjectNode> source = $(ProjectNode::getSource);

    Capture<PlanNode> filter = Capture.make();
    Capture<ProjectNode> parent = Capture.make();
    Matcher<Expression> disjunction = match(Expression.class);
    Matcher<ProjectNode> Project = match(ProjectNode.class);
    Matcher<FilterNode> Filter = match(FilterNode.class);

//    Matcher<ProjectNode> matcher = Project.as(parent)
//            .with(source.matching(Filter.as(filter)));
//                    .with($(FilterNode::getPredicate).matching(disjunction))));

    @Test
    public void trivial_matchers() {
        //any
        assertMatch(any(), null);
        assertMatch(any(), 42);
        assertMatch(any(), "John Doe");

        //class based
        assertMatch(match(Integer.class), 42);
        assertMatch(match(Number.class), 42);
        assertNoMatch(match(Integer.class), "John Doe");

        //predicate-based
        assertMatch(match(Integer.class, (x1) -> x1 > 0), 42);
        assertNoMatch(match(Integer.class, (x) -> x > 0), -1);
    }

    @Test
    public void property_matchers() {
        PropertyMatcher<String, Integer> lengthOne = $(String::length).matching(match(Integer.class, (x) -> x == 1));
        assertMatch(match(String.class).with(lengthOne), "a");
        assertNoMatch(match(String.class).with(lengthOne), "aa");
    }

    @Test
    public void match_object() {
        assertMatch(Project, new ProjectNode(null));
        assertNoMatch(Project, new FilterNode(null));

        assertNoMatch(Project, new FilterNode(null));
    }


    @Test
    public void match_property() {
        Matcher<ProjectNode> matcher = Project
                .with($(ProjectNode::getSource).matching(Filter));

        assertMatch(matcher, new ProjectNode(new FilterNode(null)));
        assertNoMatch(matcher, new FilterNode(null));
        assertNoMatch(matcher, new ProjectNode(null));
        assertNoMatch(matcher, new ProjectNode(new ProjectNode(null)));
    }

    private <T> void assertMatch(Matcher<? extends T> matcher, T expectedMatch) {
        assertTrue(matcher.matches(expectedMatch));
    }

    private <T> void assertNoMatch(Matcher<T> matcher, Object expectedNoMatch) {
        assertFalse(matcher.matches(expectedNoMatch));
    }
}