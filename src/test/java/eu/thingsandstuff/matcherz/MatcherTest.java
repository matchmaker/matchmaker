package eu.thingsandstuff.matcherz;

import example.ast.Expression;
import example.ast.FilterNode;
import example.ast.PlanNode;
import example.ast.ProjectNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static eu.thingsandstuff.matcherz.Matcher.*;
import static eu.thingsandstuff.matcherz.Property.$;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        //assertMatch(any(), null); //FIXME
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
    public void match_object() {
        assertMatch(Project, new ProjectNode(null));
        assertNoMatch(Project, new FilterNode(null));
    }

    @Test
    public void property_matchers() {
        PropertyMatcher<String, Integer> lengthOne = $(String::length).matching(match(Integer.class, (x) -> x == 1));
        assertMatch(match(String.class).with(lengthOne), "a");
        assertNoMatch(match(String.class).with(lengthOne), "aa");
    }

    @Test
    public void evidence_backed_matching_using_extractors() {
        Extractor<List<String>> stringWithVowels = Extractor.assuming(String.class, (x) -> {
            Stream<String> characters = x.chars().mapToObj(c -> String.valueOf((char) c));
            List<String> vowels = characters.filter(c -> "aeiouy".contains(c.toLowerCase())).collect(toList());
            return Optional.of(vowels).filter(l -> !l.isEmpty());
        });

        assertMatch(match(stringWithVowels), "John Doe", asList("o", "o", "e"));
        assertNoMatch(match(stringWithVowels), "pqrst");
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

    private <T> void assertMatch(Matcher<T> matcher, T expectedMatch) {
        assertMatch(matcher, expectedMatch, expectedMatch);
    }

    private <T, R> void assertMatch(Matcher<R> matcher, T matchedAgainst, R expectedMatch) {
        Optional<R> match = matcher.match(matchedAgainst);
        assertEquals(Optional.of(expectedMatch), match);
    }

    private <T> void assertNoMatch(Matcher<T> matcher, Object expectedNoMatch) {
        Optional<T> match = matcher.match(expectedNoMatch);
        assertEquals(Optional.empty(), match);
    }
}