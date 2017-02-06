package rocks.matchmaker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

//FIXME this copies MultiMatcher a bit. Deduplicate.
public class MultiMatcherReturningAll<T, R> extends Matcher<List<R>> {

    public MultiMatcherReturningAll(Map<Matcher<? extends T>, Function<T, R>> cases) {
        //TODO make this.scopeType = leastCommonSuperType(cases.keySet()*.scopeType)
        super(Object.class, createExtractor(new LinkedHashMap<>(cases)), emptyList(), null);
    }

    private static <T, R> Extractor<List<R>> createExtractor(Map<Matcher<? extends T>, Function<T, R>> cases) {
        return (object, captures) -> { //TODO we're losing incoming captures here
            Stream<Option<R>> successfulCases = MultiMatcher.successfulCases(cases, object);
            List<R> collect = successfulCases.map(Option::value).collect(toList());
            return Option.of(collect);
        };
    }
}
