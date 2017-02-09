package rocks.matchmaker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

//FIXME this copies MultiMatcher a bit. Deduplicate.
public class MultiMatcherReturningAll<R> extends Matcher<List<R>> {

    public MultiMatcherReturningAll(List<Matcher<R>> cases) {
        //TODO make this.scopeType = leastCommonSuperType(cases.keySet()*.scopeType)
        super(Object.class, createMatchFunction(new ArrayList<>(cases)), null);
    }

    private static <R> BiFunction<Object, Captures, Match<List<R>>> createMatchFunction(List<Matcher<R>> cases) {
        return (object, captures) -> {
            Stream<Match<R>> successfulCases = MultiMatcher.successfulCases(cases, object);
            //TODO we're losing captures here
            List<R> allMatches = successfulCases.map(Match::value).collect(toList());
            return Match.of(allMatches, captures)
                    .flatMap(value -> createMatch(null, allMatches, captures));
        };
    }
}
