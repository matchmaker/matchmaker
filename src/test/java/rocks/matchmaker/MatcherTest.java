package rocks.matchmaker;

import example.ast.Exchange;
import example.ast.FilterNode;
import example.ast.JoinNode;
import example.ast.PlanNode;
import example.ast.ProjectNode;
import example.ast.ScanNode;
import example.ast.SingleSourcePlanNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static rocks.matchmaker.Capture.newCapture;
import static rocks.matchmaker.Matcher.$;
import static rocks.matchmaker.Matcher.equalTo;
import static rocks.matchmaker.Matcher.isNull;
import static rocks.matchmaker.Matcher.nullable;
import static rocks.matchmaker.MatcherTest.PasswordProperty.has_digits;
import static rocks.matchmaker.MatcherTest.PasswordProperty.has_lowercase;
import static rocks.matchmaker.MatcherTest.PasswordProperty.has_uppercase;
import static rocks.matchmaker.MatcherTest.PasswordProperty.length_at_least_8;
import static rocks.matchmaker.PatternMatch.matchFor;
import static rocks.matchmaker.Property.optionalProperty;
import static rocks.matchmaker.Property.property;
import static rocks.matchmaker.Property.self;

@SuppressWarnings("WeakerAccess")
public class MatcherTest {

    Matcher<JoinNode> Join = $(JoinNode.class);

    Property<JoinNode> probe = property(JoinNode::getProbe);
    Property<JoinNode> build = property(JoinNode::getBuild);

    Matcher<PlanNode> Plan = $(PlanNode.class);
    Matcher<ProjectNode> Project = $(ProjectNode.class);
    Matcher<FilterNode> Filter = $(FilterNode.class);

    Matcher<ScanNode> Scan = $(ScanNode.class);
    Property<ScanNode> tableName = property(ScanNode::getTableName);

    Property<SingleSourcePlanNode> source = property(SingleSourcePlanNode::getSource);


    @Test
    void trivial_matchers() {
        //any
        assertMatch($(), 42);
        assertMatch($(), "John Doe");

        //equalTo
        Matcher<Integer> theAnswer = equalTo(42);
        assertMatch(theAnswer, 42);
        assertNoMatch(theAnswer, 44);
        assertNoMatch(theAnswer, null); //no exception thrown
        Throwable throwable = assertThrows(IllegalArgumentException.class, () -> equalTo(null));
        assertTrue(throwable.getMessage().contains("Use `Matcher.isNull()` instead"));

        //class based
        assertMatch($(Integer.class), 42);
        assertMatch($(Number.class), 42);
        assertNoMatch($(Integer.class), "John Doe");

        //predicate-based
        assertMatch($(Integer.class).$(x -> x > 0), 42);
        assertNoMatch($(Integer.class).$(x -> x > 0), -1);
    }

    @Test
    void match_object() {
        assertMatch(Project, new ProjectNode(null));
        assertNoMatch(Project, new ScanNode("t"));
    }

    @Test
    void property_matchers() {
        Matcher<String> aString = $(String.class);
        Property<String> length = property(String::length);
        String string = "a";

        assertMatch(aString.with(length.equalTo(1)), string);
        assertMatch(aString.with(length.matching(Integer.class, x -> x > 0)), string);
//        assertMatch(aString.with(length.matching(Extractor.assumingType(Integer.class, x -> Option.of(x.toString())))), string);
        assertMatch(aString.with(length.matching($())), string);
        assertMatch(aString.with(self().equalTo(string)), string);

        assertNoMatch(aString.with(length.equalTo(0)), string);
        assertNoMatch(aString.with(length.matching(Integer.class, x -> x < 1)), string);
//        assertNoMatch(aString.with(length.matching(assumingType(Integer.class, x -> Option.empty()))), string);
        assertNoMatch(aString.with(length.matching($(Void.class))), string);
        assertNoMatch(aString.with(self().equalTo("b")), string);
    }

    @Test
    void match_nested_properties() {
        Matcher<ProjectNode> matcher = Project
                .with(property(ProjectNode::getSource).matching(Scan));

        assertMatch(matcher, new ProjectNode(new ScanNode("t")));
        assertNoMatch(matcher, new ScanNode("t"));
        assertNoMatch(matcher, new ProjectNode(null));
        assertNoMatch(matcher, new ProjectNode(new ProjectNode(null)));
    }

    @Test
    void match_additional_properties() {
        Capture<List<String>> lowercase = newCapture();

        String matchedValue = "A little string.";

        Matcher<String> matcher = $(String.class)
                .$(s -> s.startsWith("A"))
                .$((CharSequence s) -> s.length() > 0)
                .$(endsWith("string."))
                .matching(hasLowercaseChars.capturedAs(lowercase));

        Match<String> match = assertMatch(matcher, matchedValue, "string.");
        assertEquals(match.capture(lowercase), characters("string.").collect(toList()));
    }

    private Extractor<String, String> endsWith(String suffix) {
        return (string, captures) -> Option.of(suffix).filter(__ -> string.endsWith(suffix));
    }

    private Matcher<List<String>> hasLowercaseChars = $(String.class).$((string, captures) -> {
        List<String> lowercaseChars = characters(string).filter(this::isLowerCase).collect(toList());
        return Option.of(lowercaseChars).filter(l -> !l.isEmpty());
    });

    private boolean isLowerCase(String string) {
        return string.toLowerCase().equals(string);
    }

    @Test
    void optional_properties() {
        Property<PlanNode> onlySource = optionalProperty(node ->
                Option.of(node.getSources())
                        .filter(sources -> sources.size() == 1)
                        .map(sources -> sources.get(0)));

        Matcher<PlanNode> planNodeWithExactlyOneSource = $(PlanNode.class)
                .with(onlySource.matching($()));

        assertMatch(planNodeWithExactlyOneSource, new ProjectNode(new ScanNode("t")));
        assertNoMatch(planNodeWithExactlyOneSource, new ScanNode("t"));
        assertNoMatch(planNodeWithExactlyOneSource, new JoinNode(new ScanNode("t"), new ScanNode("t")));
    }

    @Test
    void capturing_matches_in_a_typesafe_manner() {
        Capture<FilterNode> filter = newCapture();
        Capture<ScanNode> scan = newCapture();

        Matcher<ProjectNode> matcher = Project
                .with(source.matching(Filter.capturedAs(filter)
                        .with(source.matching(Scan.capturedAs(scan)
                                .with(tableName.equalTo("orders"))))));

        ProjectNode tree = new ProjectNode(new FilterNode(new ScanNode("orders"), null));

        Match<ProjectNode> match = assertMatch(matcher, tree);
        //notice the concrete type despite no casts:
        FilterNode capturedFilter = match.capture(filter);
        assertEquals(tree.getSource(), capturedFilter);
        assertEquals(((FilterNode) tree.getSource()).getSource(), match.capture(scan));
    }

    @Test
    void evidence_backed_matching_using_extractors() {
        Matcher<List<String>> stringWithVowels = $(String.class).$((x, captures) -> {
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
        Matcher<Void> matcher = $(Void.class).capturedAs(impossible);

        Match<Void> match = matcher.match(42);

        assertTrue(match.isEmpty());
        Throwable throwable = assertThrows(NoSuchElementException.class, () -> match.capture(impossible));
        assertTrue(() -> throwable.getMessage().contains("Empty match contains no value"));
    }

    @Test
    void unknown_capture_is_an_error() {
        Matcher<?> matcher = $();
        Capture<?> unknownCapture = newCapture();

        Match<?> match = matcher.match(42);

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

        Matcher<List<PlanNode>> accessingTheDesiredCaptures = $(PlanNode.class).$((node, params) ->
                Option.of(asList(
                        params.get(left), params.get(right), params.get(root), params.get(parent)
                )));

        Matcher<JoinNode> matcher = Join.capturedAs(root)
                .with(probe.matching(Join.capturedAs(parent)
                        .with(probe.matching(Scan.capturedAs(left)))
                        .with(build.matching(Scan.capturedAs(right)))))
                .with(build.matching(Scan
                        .matching(accessingTheDesiredCaptures.capturedAs(caputres))));

        ScanNode expectedLeft = new ScanNode("a");
        ScanNode expectedRight = new ScanNode("b");
        JoinNode expectedParent = new JoinNode(expectedLeft, expectedRight);
        JoinNode expectedRoot = new JoinNode(expectedParent, new ScanNode("c"));

        Match<JoinNode> match = assertMatch(matcher, expectedRoot);
        assertEquals(match.capture(caputres), asList(expectedLeft, expectedRight, expectedRoot, expectedParent));
    }

    //TODO add negative cases to the below
    @Test
    void pattern_matching_for_single_result() {
        //We restrict the PatternMatch result type (here: Integer)
        //to achieve type safety in return type of the '.returns(...)' part.
        //We also restrict the type of the matchers used in cases (here: PlanNode)
        //to achieve type safety in input type of the '.returns(...)' part.
        Matcher<Integer> sourcesNumber = matchFor(PlanNode.class, Integer.class)
                .caseOf(Scan).returns(() -> 0)
                .caseOf(Filter).returns(() -> 1)
                .caseOf(Project).returns(() -> 1)
                .caseOf(Join).returns(() -> 2)
                .caseOf(Plan).returns(node -> node.getSources().size())
                .returnFirst();

        assertMatch(sourcesNumber, new ScanNode("t"), 0);
        assertMatch(sourcesNumber, new FilterNode(null, null), 1);
        assertMatch(sourcesNumber, new ProjectNode(null), 1);
        assertMatch(sourcesNumber, new JoinNode(null, null), 2);
        assertMatch(sourcesNumber, new Exchange(null, null, null), 3);
    }

    @Test
    void pattern_matching_for_multiple_results() {
        //Restricting the matcher return type (here: String)
        //also allows for easier definition of cases using predicates.
        Matcher<List<PasswordProperty>> passwordProperties = matchFor(String.class, PasswordProperty.class)
                .caseOf(s -> s.matches(".*?[A-Z].*")).returns(() -> has_uppercase)
                .caseOf(s -> s.matches(".*?[a-z].*")).returns(() -> has_lowercase)
                .caseOf(s -> s.matches(".*?[0-9].*")).returns(() -> has_digits)
                .caseOf(s -> s.length() >= 8).returns(() -> length_at_least_8)
                .returningAll();

        assertMatch(passwordProperties, "", emptyList()); //TODO this, or noMatch?
        assertMatch(passwordProperties, "foobar", asList(has_lowercase));
        assertMatch(passwordProperties, "FooBar", asList(has_uppercase, has_lowercase));
        assertMatch(passwordProperties, "1234567890", asList(has_digits, length_at_least_8));
        assertMatch(passwordProperties,
                "aProperPassword111", asList(has_uppercase, has_lowercase, has_digits, length_at_least_8));
    }

    enum PasswordProperty {
        has_uppercase, has_lowercase, has_digits, length_at_least_8
    }

    @Test
    void pattern_matching_for_single_result_with_captures() {
        Capture<ScanNode> scanNode = newCapture();

        Matcher<PlanNode> joinMatcher = matchFor(PlanNode.class, PlanNode.class)
                .caseOf(Join
                        .with(probe.matching(Scan
                                .capturedAs(scanNode)))
                )
                .returns(Function.identity())
                .caseOf(Join
                        .with(build.matching(Scan
                                .capturedAs(scanNode)))
                )
                .returns(Function.identity())
                .returnFirst();

        ScanNode scan = new ScanNode("t");
        Match<PlanNode> first = assertMatch(joinMatcher, new JoinNode(scan, null));
        assertEquals(scan, first.capture(scanNode));
        Match<PlanNode> second = assertMatch(joinMatcher, new JoinNode(null, scan));
        assertEquals(scan, second.capture(scanNode));
    }

    @Test
    void narrows_down_tried_patterns_based_on_scope_type() {
        List<Class<?>> matchAttempts = new ArrayList<>();
        PatternMatch<Object, Object> patternMatch = matchFor(Object.class, Object.class)
                .caseOf(registerMatch(Void.class, matchAttempts)).returns(Function.identity())
                .caseOf(registerMatch(String.class, matchAttempts)).returns(Function.identity())
                .caseOf(registerMatch(Integer.class, matchAttempts)).returns(Function.identity())
                .caseOf(registerMatch(Number.class, matchAttempts)).returns(Function.identity())
                .caseOf(registerMatch(Double.class, matchAttempts)).returns(Function.identity())
                .caseOf(registerMatch(CharSequence.class, matchAttempts)).returns(Function.identity())
                .caseOf(registerMatch(String.class, matchAttempts)).returns(Function.identity());

        assertMatchAttempts(patternMatch.returnFirst(), 42, matchAttempts, Integer.class);
        assertMatchAttempts(patternMatch.returnFirst(), 0.1, matchAttempts, Number.class);
        assertMatchAttempts(patternMatch.returnFirst(), "foo", matchAttempts, String.class);
        assertMatchAttempts(patternMatch.returningAll(), 42, matchAttempts, Integer.class, Number.class);
        assertMatchAttempts(patternMatch.returningAll(), 0.1, matchAttempts, Number.class, Double.class);
        assertMatchAttempts(patternMatch.returningAll(), "foo", matchAttempts, String.class, CharSequence.class, String.class);
        assertMatchAttempts(patternMatch.returnFirst(), null, matchAttempts, Void.class);
        assertMatchAttempts(patternMatch.returningAll(), null, matchAttempts,
                Void.class, String.class, Integer.class, Number.class, Double.class, CharSequence.class, String.class);
    }

    private <T> Matcher<T> registerMatch(Class<T> scopeClass, List<Class<?>> matchAttemtpts) {
        return nullable(scopeClass).$((x, captures) -> {
            matchAttemtpts.add(scopeClass);
            return Option.of(x);
        });
    }

    private void assertMatchAttempts(
            Matcher<?> matcher,
            Object matchedObject,
            List<Class<?>> matchAttempts,
            Class<?>... expectedMatchAttempts
    ) {
        matcher.match(matchedObject);
        assertEquals(asList(expectedMatchAttempts), matchAttempts);
        matchAttempts.clear();
    }

    @Test
    void null_not_matched_by_default() {
        assertNoMatch($(), null);
        assertNoMatch($(Integer.class), null);

        //the predefined isNull matcher works as expected:
        assertMatch(isNull(), null);
        assertNoMatch(isNull(), 42);

        //nulls can be matched using the `nullable` matcher factory method as a  starting point
        assertMatch(nullable(Integer.class), null);
        assertMatch(nullable(Integer.class), 42);

        //the nullable matchers work as expected when chained with predicates
        assertMatch(nullable(String.class).$(value -> "John Doe".equals(value)), "John Doe");
        assertNoMatch(nullable(String.class).$(value -> "John Doe".equals(value)), null);

        //one has to be careful when basing off nullable matchers
        assertThrows(NullPointerException.class, () ->
                assertMatch(nullable(String.class).$(x -> x.length() > 0), null));
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
