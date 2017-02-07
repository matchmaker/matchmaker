package rocks.matchmaker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

//FIXME this copies MultiMatcher a bit. Deduplicate.
public class MultiMatcherReturningAll<T, R> extends Matcher<List<R>> {

    public MultiMatcherReturningAll(Map<Matcher<? extends T>, Function<T, R>> cases) {
        //TODO make this.scopeType = leastCommonSuperType(cases.keySet()*.scopeType)
        super(Object.class, createMatchFunction(new LinkedHashMap<>(cases)), null);
    }

    private static <T, R> BiFunction<Object, Captures, Match<List<R>>> createMatchFunction(Map<Matcher<? extends T>, Function<T, R>> cases) {
        return (object, captures) -> {
            Stream<Match<R>> successfulCases = MultiMatcher.successfulCases(cases, object);
            //TODO we're losing captures here
            List<R> allMatches = successfulCases.map(Match::value).collect(toList());
            return Match.of(allMatches, captures)
                    .flatMap(value -> createMatch(null, allMatches, captures));
        };
    }
}
