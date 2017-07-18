package rocks.matchmaker;

import example.ast.FilterNode;
import example.ast.JoinNode;
import example.ast.PlanNode;
import example.ast.ProjectNode;
import example.ast.ScanNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static example.ast.Patterns.build;
import static example.ast.Patterns.filter;
import static example.ast.Patterns.join;
import static example.ast.Patterns.plan;
import static example.ast.Patterns.probe;
import static example.ast.Patterns.project;
import static example.ast.Patterns.scan;
import static example.ast.Patterns.source;
import static example.ast.Patterns.tableName;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static rocks.matchmaker.Capture.newCapture;
import static rocks.matchmaker.DefaultMatcher.DEFAULT_MATCHER;
import static rocks.matchmaker.Pattern.any;
import static rocks.matchmaker.Pattern.equalTo;
import static rocks.matchmaker.Pattern.typeOf;
import static rocks.matchmaker.Property.optionalProperty;
import static rocks.matchmaker.Property.property;
import static rocks.matchmaker.Property.self;

@SuppressWarnings("WeakerAccess")
public class MatcherTest {

    @Test
    void trivial_matchers() {
        //any
        assertMatch(any(), 42);
        assertMatch(any(), "John Doe");

        //equalTo
        Pattern<Integer> theAnswer = equalTo(42);
        assertMatch(theAnswer, 42);
        assertNoMatch(theAnswer, 44);
        assertNoMatch(theAnswer, null); //no exception thrown
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> equalTo(null));
        assertTrue(throwable.getMessage().contains("Use `Pattern.isNull()` instead"));

        //class based
        assertMatch(typeOf(Integer.class), 42);
        assertMatch(typeOf(Number.class), 42);
        assertNoMatch(typeOf(Integer.class), "John Doe");

        //predicate-based
        assertMatch(typeOf(Integer.class).matching(x -> x > 0), 42);
        assertNoMatch(typeOf(Integer.class).matching(x -> x > 0), -1);
    }

    @Test
    void equalTo_cant_assert_type() {
        assertMatch(equalTo(new ArrayList<Integer>()), new LinkedList<Integer>());
    }

    @Test
    void match_object() {
        assertMatch(project(), new ProjectNode(null));
        assertNoMatch(project(), new ScanNode("t"));
    }

    @Test
    void property_matchers() {
        Pattern<String> aString = typeOf(String.class);
        Property<String, Integer> length = property(String::length);
        String string = "a";

        assertMatch(aString.with(length.equalTo(1)), string);
        assertMatch(project().with(source().ofType(ScanNode.class)), new ProjectNode(new ScanNode("T")));
        assertMatch(aString.with(length.matching(x -> x > 0)), string);
        assertMatch(aString.with(length.matching((Number x) -> x.intValue() > 0)), string);
        assertMatch(aString.with(length.matching((x, captures) -> Option.of(x.toString()))), string);
        assertMatch(aString.with(length.matching((x, captures) -> Option.of(x.toString()))), string);
        assertMatch(aString.with(length.matching(any())), string);
        assertMatch(aString.with(self().equalTo(string)), string);

        assertNoMatch(aString.with(length.equalTo(0)), string);
        assertNoMatch(project().with(source().ofType(ScanNode.class)), new ProjectNode(new ProjectNode(new ScanNode("T"))));
        assertNoMatch(aString.with(length.matching(x -> x < 1)), string);
        assertNoMatch(aString.with(length.matching((Number x) -> x.intValue() < 1)), string);
        assertNoMatch(aString.with(length.matching((x, captures) -> Option.empty())), string);
        assertNoMatch(aString.with(length.matching(typeOf(Void.class))), string);
        assertNoMatch(aString.with(self().equalTo("b")), string);
    }

    @Test
    void match_nested_properties() {
        Pattern<ProjectNode> pattern = project().with(source().matching(scan()));

        assertMatch(pattern, new ProjectNode(new ScanNode("t")));
        assertNoMatch(pattern, new ScanNode("t"));
        assertNoMatch(pattern, new ProjectNode(null));
        assertNoMatch(pattern, new ProjectNode(new ProjectNode(null)));
    }

    @Test
    void match_additional_properties() {
        Capture<List<String>> lowercase = newCapture();

        String matchedValue = "A little string.";

        Pattern<List<String>> pattern = typeOf(String.class)
                .matching(s -> s.startsWith("A"))
                .matching((CharSequence s) -> s.length() > 0)
                .matching(endsWith("string."))
                .matching((value, captures) -> Option.of(value).filter(v -> v.trim().equals(v)))
                .matching(hasLowercaseChars.capturedAs(lowercase));

        List<String> lowercaseChars = characters("string.").collect(toList());
        Match<List<String>> match = assertMatch(pattern, matchedValue, lowercaseChars);
        assertEquals(match.capture(lowercase), lowercaseChars);
    }

    private Extractor<String, String> endsWith(String suffix) {
        return (string, captures) -> Option.of(suffix).filter(__ -> string.endsWith(suffix));
    }

    private Pattern<List<String>> hasLowercaseChars = typeOf(String.class).matching((string, captures) -> {
        List<String> lowercaseChars = characters(string).filter(this::isLowerCase).collect(toList());
        return Option.of(lowercaseChars).filter(l -> !l.isEmpty());
    });

    private boolean isLowerCase(String string) {
        return string.toLowerCase().equals(string);
    }

    @Test
    void optional_properties() {
        Property<PlanNode, PlanNode> onlySource = optionalProperty(node ->
                Option.of(node.getSources())
                        .filter(sources -> sources.size() == 1)
                        .map(sources -> sources.get(0)));

        Pattern<PlanNode> planNodeWithExactlyOneSource = plan()
                .with(onlySource.matching(any()));

        assertMatch(planNodeWithExactlyOneSource, new ProjectNode(new ScanNode("t")));
        assertNoMatch(planNodeWithExactlyOneSource, new ScanNode("t"));
        assertNoMatch(planNodeWithExactlyOneSource, new JoinNode(new ScanNode("t"), new ScanNode("t")));
    }

    @Test
    void capturing_matches_in_a_typesafe_manner() {
        Capture<FilterNode> filter = newCapture();
        Capture<ScanNode> scan = newCapture();
        Capture<String> name = newCapture();

        Pattern<ProjectNode> pattern = project()
                .with(source().matching(filter().capturedAs(filter)
                        .with(source().matching(scan().capturedAs(scan)
                                .with(tableName().capturedAs(name))))));

        ProjectNode tree = new ProjectNode(new FilterNode(new ScanNode("orders"), null));

        Match<ProjectNode> match = assertMatch(pattern, tree);
        //notice the concrete type despite no casts:
        FilterNode capturedFilter = match.capture(filter);
        assertEquals(tree.getSource(), capturedFilter);
        assertEquals(((FilterNode) tree.getSource()).getSource(), match.capture(scan));
        assertEquals("orders", match.capture(name));
    }

    @Test
    void evidence_backed_matching_using_extractors() {
        Pattern<List<String>> stringWithVowels = typeOf(String.class).matching((x, captures) -> {
            List<String> vowels = characters(x).filter(c -> "aeiouy".contains(c.toLowerCase())).collect(toList());
            return Option.of(vowels).filter(l -> !l.isEmpty());
        });

        Capture<List<String>> vowels = newCapture();

        Match<List<String>> match = assertMatch(stringWithVowels.capturedAs(vowels), "John Doe", asList("o", "o", "e"));
        assertEquals(match.value(), match.capture(vowels));

        assertNoMatch(stringWithVowels, "pqrst");
    }

    private Stream<String> characters(String string) {
        return string.chars().mapToObj(c -> String.valueOf((char) c));
    }

    @Test
    void no_match_means_no_captures() {
        Capture<Void> impossible = newCapture();
        Pattern<Void> pattern = typeOf(Void.class).capturedAs(impossible);

        Match<Void> match = DEFAULT_MATCHER.match(pattern, 42);

        assertTrue(match.isEmpty());
        Throwable throwable = assertThrows(NoSuchElementException.class, () -> match.capture(impossible));
        assertTrue(() -> throwable.getMessage().contains("Empty match contains no value"));
    }

    @Test
    void unknown_capture_is_an_error() {
        Pattern<?> pattern = any();
        Capture<?> unknownCapture = newCapture();

        Match<?> match = DEFAULT_MATCHER.match(pattern, 42);

        Throwable throwable = assertThrows(NoSuchElementException.class, () -> match.capture(unknownCapture));
        assertTrue(() -> throwable.getMessage().contains("unknown Capture"));
        //TODO make the error message somewhat help which capture was used, when the captures are human-discernable.
    }

    @Test
    void extractors_parameterized_with_captures() {
        Capture<JoinNode> root = newCapture();
        Capture<JoinNode> parent = newCapture();
        Capture<ScanNode> left = newCapture();
        Capture<ScanNode> right = newCapture();
        Capture<List<PlanNode>> caputres = newCapture();

        Pattern<List<PlanNode>> accessingTheDesiredCaptures = plan().matching((node, params) ->
                Option.of(asList(
                        params.get(left), params.get(right), params.get(root), params.get(parent)
                )));

        Pattern<JoinNode> pattern = join().capturedAs(root)
                .with(probe().matching(join().capturedAs(parent)
                        .with(probe().matching(scan().capturedAs(left)))
                        .with(build().matching(scan().capturedAs(right)))))
                .with(build().matching(scan()
                        .matching(accessingTheDesiredCaptures.capturedAs(caputres))));

        System.out.println(pattern);

        ScanNode expectedLeft = new ScanNode("a");
        ScanNode expectedRight = new ScanNode("b");
        JoinNode expectedParent = new JoinNode(expectedLeft, expectedRight);
        JoinNode expectedRoot = new JoinNode(expectedParent, new ScanNode("c"));

        Match<JoinNode> match = assertMatch(pattern, expectedRoot);
        assertEquals(asList(expectedLeft, expectedRight, expectedRoot, expectedParent), match.capture(caputres));
    }

    @Test
    void null_not_matched_by_default() {
        assertNoMatch(any(), null);
        assertNoMatch(typeOf(Integer.class), null);
    }

    private <T> Match<T> assertMatch(Pattern<T> pattern, T expectedMatch) {
        return assertMatch(pattern, expectedMatch, expectedMatch);
    }

    private <T, R> Match<R> assertMatch(Pattern<R> pattern, T matchedAgainst, R expectedMatch) {
        Match<R> match = DEFAULT_MATCHER.match(pattern, matchedAgainst);
        assertEquals(expectedMatch, match.value());
        return match;
    }

    private <T> void assertNoMatch(Pattern<T> pattern, Object expectedNoMatch) {
        Match<T> match = DEFAULT_MATCHER.match(pattern, expectedNoMatch);
        assertEquals(Match.empty(), match);
    }

}
