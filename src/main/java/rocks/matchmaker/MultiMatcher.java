package rocks.matchmaker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class MultiMatcher<R> extends Matcher<R> {

    public MultiMatcher(List<Matcher<R>> cases) {
        //TODO make this.scopeType = leastCommonSuperType(cases.keySet()*.scopeType)
        super(Object.class, createMatchFunction(new ArrayList<>(cases)), null);
    }

    private static <R> BiFunction<Object, Captures, Match<R>> createMatchFunction(List<Matcher<R>> cases) {
        return (object, captures) -> {
            Stream<Match<R>> successfulCases = successfulCases(cases, object);
            Match<R> caseResult = successfulCases.findFirst().orElse(Match.empty());
            return caseResult;
        };
    }

    static <R> Stream<Match<R>> successfulCases(List<Matcher<R>> cases, Object object) {
        Stream<Match<R>> caseResults = cases.stream().map(matcher -> matcher.match(object));
        return caseResults.filter(Match::isPresent);
    }
}
