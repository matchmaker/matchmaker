package rocks.matchmaker;

import com.google.common.collect.SortedSetMultimap;
import rocks.matchmaker.util.Indexed;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static rocks.matchmaker.MultiMatcher.indexByScopeType;
import static rocks.matchmaker.MultiMatcher.successfulCases;

//FIXME this copies MultiMatcher a bit. Deduplicate.
public class MultiMatcherReturningAll<R> extends Matcher<List<R>> {

    public MultiMatcherReturningAll(List<Matcher<R>> cases) {
        //TODO make this.scopeType = leastCommonSuperType(cases.keySet()*.scopeType)
        super(Object.class, createMatchFunction(new ArrayList<>(cases)), null);
    }

    private static <R> BiFunction<Object, Captures, Match<List<R>>> createMatchFunction(List<Matcher<R>> cases) {
        SortedSetMultimap<Class<?>, Indexed<Matcher<R>>> matchersByScopeType = indexByScopeType(cases);
        return (object, captures) -> {
            Stream<Match<R>> successfulCases = successfulCases(cases, matchersByScopeType, object);
            //TODO we're losing captures here
            List<R> allMatches = successfulCases.map(Match::value).collect(toList());
            return Match.of(allMatches, captures)
                    .flatMap(value -> createMatch(null, allMatches, captures));
        };
    }
}
