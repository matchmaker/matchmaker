package rocks.matchmaker.pattern;

import rocks.matchmaker.Captures;
import rocks.matchmaker.Match;
import rocks.matchmaker.Matcher;
import rocks.matchmaker.Option;
import rocks.matchmaker.Pattern;
import rocks.matchmaker.PatternVisitor;
import rocks.matchmaker.PropertyPattern;

import java.util.function.Function;

public class WithPattern<T> extends Pattern<T> {

    private final PropertyPattern<? super T, ?> propertyPattern;

    public WithPattern(PropertyPattern<? super T, ?> propertyPattern, Pattern<T> previous) {
        super(previous);
        this.propertyPattern = propertyPattern;
    }

    public Pattern<?> getPattern() {
        return propertyPattern.getPattern();
    }

    public Function<? super T, Option<?>> getProperty() {
        return propertyPattern.getProperty();
    }

    @Override
    public Match<T> accept(Matcher matcher, Object object, Captures captures) {
        return matcher.evaluate(this, object, captures);
    }

    @Override
    public void accept(PatternVisitor patternVisitor) {
        patternVisitor.visit(this);
    }
}
