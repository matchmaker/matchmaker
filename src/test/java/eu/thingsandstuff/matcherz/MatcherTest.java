package eu.thingsandstuff.matcherz;

import example.ast.FilterNode;
import example.ast.ProjectNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static eu.thingsandstuff.matcherz.Capture.newCapture;
import static eu.thingsandstuff.matcherz.Matcher.any;
import static eu.thingsandstuff.matcherz.Matcher.match;
import static eu.thingsandstuff.matcherz.Property.$;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("WeakerAccess")
public class MatcherTest {

    Matcher<ProjectNode> Project = match(ProjectNode.class);
    Property<ProjectNode> source = $(ProjectNode::getSource);

    Matcher<FilterNode> Filter = match(FilterNode.class);

    @Test
    void trivial_matchers() {
        //any
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
    void match_object() {
        assertMatch(Project, new ProjectNode(null));
        assertNoMatch(Project, new FilterNode(null));
    }

    @Test
    void property_matchers() {
        PropertyMatcher<String, Integer> lengthOne = $(String::length).matching(match(Integer.class, (x) -> x == 1));
        assertMatch(match(String.class).with(lengthOne), "a");
        assertNoMatch(match(String.class).with(lengthOne), "aa");
    }

    @Test
    void capturing_matches_in_a_typesafe_manner() {
        Capture<ProjectNode> root = newCapture();
        Capture<ProjectNode> child = newCapture();
        Capture<FilterNode> filter = newCapture();

        Matcher<ProjectNode> matcher =
                Project.as(root)
                        .with(source.matching(Project.as(child)
                                .with(source.matching(Filter.as(filter)))));

        ProjectNode tree = new ProjectNode(new ProjectNode(new FilterNode(null)));

        Match<ProjectNode> match = assertMatch(matcher, tree);
        ProjectNode capturedRoot = match.capture(root);
        assertEquals(tree, capturedRoot);
        assertEquals(tree.getSource(), match.capture(child));
        assertEquals(((ProjectNode) tree.getSource()).getSource(), match.capture(filter));
    }

    @Test
    void evidence_backed_matching_using_extractors() {
        Extractor<List<String>> stringWithVowels = Extractor.assuming(String.class, (x) -> {
            Stream<String> characters = x.chars().mapToObj(c -> String.valueOf((char) c));
            List<String> vowels = characters.filter(c -> "aeiouy".contains(c.toLowerCase())).collect(toList());
            return Match.of(vowels).filter(l -> !l.isEmpty());
        });
        Matcher<List<String>> matcher = match(stringWithVowels);

        Capture<List<String>> vowels = newCapture();
        List<String> expectedVowels = asList("o", "o", "e");

        Match<List<String>> match = assertMatch(matcher.as(vowels), "John Doe", expectedVowels);
        assertEquals(expectedVowels, match.capture(vowels));

        assertNoMatch(matcher, "pqrst");
    }

    @Test
    void no_match_means_no_captures() {
        Capture<Void> impossible = newCapture();
        Matcher<Void> matcher = match(Void.class).as(impossible);

        Match<Void> match = matcher.match(42);

        assertTrue(match.isEmpty());
        Throwable throwable = assertThrows(NoSuchElementException.class, () -> match.capture(impossible));
        assertTrue(() -> throwable.getMessage().contains("empty match"));
    }

    @Test
    void unknown_capture_is_an_error() {
        Matcher<?> matcher = any();
        Capture<?> unknownCapture = newCapture();

        Match<?> match = matcher.match(42);

        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> match.capture(unknownCapture));
        assertTrue(() -> throwable.getMessage().contains("This capture is unknown to this matcher"));
        //TODO make the error message somewhat help which capture was used, when the captures are human-discernable.
    }

    @Test
    void match_property() {
        Matcher<ProjectNode> matcher = Project
                .with($(ProjectNode::getSource).matching(Filter));

        assertMatch(matcher, new ProjectNode(new FilterNode(null)));
        assertNoMatch(matcher, new FilterNode(null));
        assertNoMatch(matcher, new ProjectNode(null));
        assertNoMatch(matcher, new ProjectNode(new ProjectNode(null)));
    }

    @Test
    void null_not_matched_by_default() {
        assertNoMatch(any(), null);
        assertNoMatch(match(Integer.class), null);

        //nulls can be matched using a custom extractor for now
        Extractor<Object> nullAcceptingExtractor = (x) -> Match.of(x);
        assertMatch(match(nullAcceptingExtractor), null);
    }

    private <T> Match<T> assertMatch(Matcher<T> matcher, T expectedMatch) {
        return assertMatch(matcher, expectedMatch, expectedMatch);
    }

    private <T, R> Match<R> assertMatch(Matcher<R> matcher, T matchedAgainst, R expectedMatch) {
        Match<R> match = matcher.match(matchedAgainst);
        assertEquals(expectedMatch, match.value());
        return match;
    }

    private <T> void assertNoMatch(Matcher<T> matcher, Object expectedNoMatch) {
        Match<T> match = matcher.match(expectedNoMatch);
        assertEquals(Match.empty(), match);
    }
}