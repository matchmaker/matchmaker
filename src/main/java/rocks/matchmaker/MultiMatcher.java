package rocks.matchmaker;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public class MultiMatcher<T, R> extends Matcher<R> {

    public MultiMatcher(Map<Matcher<? extends T>, Function<T, R>> cases) {
        //TODO make this.scopeType = leastCommonSuperType(cases.keySet()*.scopeType)
        super(Object.class, createExtractor(new LinkedHashMap<>(cases)), emptyList(), null);
    }

    private static <T, R> Extractor<R> createExtractor(Map<Matcher<? extends T>, Function<T, R>> cases) {
        return (object, captures) -> { //TODO we're losing incoming captures here
            Stream<Option<R>> successfulCases = successfulCases(cases, object);
            Option<R> caseResult = successfulCases.findFirst().orElse(Option.empty());
            return caseResult;
        };
    }

    static <T, R> Stream<Option<R>> successfulCases(Map<Matcher<? extends T>, Function<T, R>> cases, Object object) {
        Stream<Option<R>> caseResults = cases.entrySet().stream().map(patternCase -> {
            Matcher<T> castMatcher = covariantUpcast(patternCase.getKey());
            Match<T> match = castMatcher.match(object);
            Function<T, R> matchToCaseResultMapper = patternCase.getValue();
            //TODO we're losing outgoing captures here
            return match.isPresent() ? Option.of(match.value()).map(matchToCaseResultMapper) : Option.empty();
        });
        return caseResults.filter(Option::isPresent);
    }
}
