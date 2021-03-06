package rocks.matchmaker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static rocks.matchmaker.Matcher.nullable;
import static rocks.matchmaker.Matcher.typeOf;

public class PatternMatch<T, R> {

    private Class<T> matcherResultType;
    private Class<R> caseResultType;

    private List<Matcher<R>> cases = new ArrayList<>();

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
        return caseOf(typeOf(this.matcherResultType).matching(predicate));
    }

    public Case<T, R> caseOf(Matcher<? extends T> matcher) {
        return new Case<T, R> (){

            @Override
            public PatternMatch<T, R> returns(Function<T, R> result) {
                //TODO rewrite this so that immutable objects are used
                //TODO: replace with matcher.map(result)
                Matcher<R> resultMatcher = matcher.flatMap((match, captures) -> Match.of(result.apply(match), captures));
                PatternMatch.this.cases.add(resultMatcher);
                return PatternMatch.this;
            }
        };
    }

    public Matcher<R> returnFirst() {
        return nullable(Object.class).flatMap(MultiMatcherMatchFunctions.returnFirst(cases));
    }

    public Matcher<List<R>> returningAll() {
        return nullable(Object.class).flatMap(MultiMatcherMatchFunctions.returnAll(cases));
    }

    public interface Case<T, R> {

        default PatternMatch<T, R> returns(Supplier<R> result) {
            return returns(__ -> result.get());
        }

        PatternMatch<T, R> returns(Function<T, R> result);
    }
}
