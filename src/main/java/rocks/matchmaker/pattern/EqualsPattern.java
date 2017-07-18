package rocks.matchmaker.pattern;

import rocks.matchmaker.Captures;
import rocks.matchmaker.Match;
import rocks.matchmaker.Matcher;
import rocks.matchmaker.Pattern;
import rocks.matchmaker.PatternVisitor;

public class EqualsPattern<T> extends Pattern<T> {

    private final T expectedValue;

    public EqualsPattern(T expectedValue) {
        this.expectedValue = expectedValue;
    }

    public T expectedValue() {
        return expectedValue;
    }

    @Override
    public Match<T> accept(Matcher matcher, Object object, Captures captures) {
        return matcher.evaluate(this, object, captures);
    }

    @Override
    public <R> void accept(PatternVisitor<R> patternVisitor) {
        patternVisitor.visit(this);
    }
}
