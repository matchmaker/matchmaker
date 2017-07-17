package rocks.matchmaker;

import rocks.matchmaker.pattern.CapturePattern;
import rocks.matchmaker.pattern.CombinePattern;
import rocks.matchmaker.pattern.EqualsPattern;
import rocks.matchmaker.pattern.ExtractPattern;
import rocks.matchmaker.pattern.FilterPattern;
import rocks.matchmaker.pattern.TypeOfPattern;
import rocks.matchmaker.pattern.WithPattern;

public interface Matcher {

    default <T> Match<T> match(Pattern<T> pattern, Object object) {
        return match(pattern, object, Captures.empty());
    }

    <T> Match<T> match(Pattern<T> pattern, Object object, Captures captures);

    <T> Match<T> evaluate(CapturePattern<T> capturePattern, Object object, Captures captures);

    <T> Match<T> evaluate(CombinePattern<T> combinePattern, Object object, Captures captures);

    <T> Match<T> evaluate(EqualsPattern<T> equalsPattern, Object object, Captures captures);

    <T, R> Match<R> evaluate(ExtractPattern<T, R> extractPattern, Object object, Captures captures);

    <T> Match<T> evaluate(FilterPattern<T> filterPattern, Object object, Captures captures);

    <T> Match<T> evaluate(TypeOfPattern<T> typeOfPattern, Object object, Captures captures);

    <T> Match<T> evaluate(WithPattern<T> withPattern, Object object, Captures captures);
}
