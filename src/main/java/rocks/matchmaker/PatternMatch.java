package rocks.matchmaker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static rocks.matchmaker.Matcher.matcher;

public class PatternMatch<T, R> {

    private Class<T> matcherResultType;
    private Class<R> caseResultType;

    private Map<Matcher<? extends T>, Function<T, R>> cases = new LinkedHashMap<>();

    private PatternMatch(Class<T> matcherResultType, Class<R> caseResultType) {
        this.matcherResultType = matcherResultType;
        this.caseResultType = caseResultType;
    }

    public static <R> PatternMatch<Object, R> matchFor(Class<R> caseResultType) {
        return matchFor(Object.class, caseResultType);
    }

    public static <T, R> PatternMatch<T, R> matchFor(Class<T> matcherResultType, Class<R> caseResultType) {
        return new PatternMatch<>(matcherResultType, caseResultType);
    }

    public Case<T, R> caseOf(Predicate<T> predicate) {
        return caseOf(matcher(this.matcherResultType, predicate));
    }

    //TODO add Matcher.mapping and Matcher.flatMapping and use a List<Matcher<R>> instead of the 'cases' Map
    public Case<T, R> caseOf(Matcher<? extends T> matcher) {
        return new Case<T, R> (){

            @Override
            public PatternMatch<T, R> returns(Function<T, R> result) {
                //TODO rewrite this so that immutable objects are used
                PatternMatch.this.cases.put(matcher, result);
                return PatternMatch.this;
            }
        };
    }

    public Matcher<R> returnFirst() {
        return new MultiMatcher<>(cases);
    }

    public Matcher<List<R>> returningAll() {
        return new MultiMatcherReturningAll<>(cases);
    }

    public interface Case<T, R> {

        default PatternMatch<T, R> returns(Supplier<R> result) {
            return returns(__ -> result.get());
        }

        PatternMatch<T, R> returns(Function<T, R> result);
    }
}
