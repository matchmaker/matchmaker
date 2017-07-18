package rocks.matchmaker.pattern;

import rocks.matchmaker.Captures;
import rocks.matchmaker.Match;
import rocks.matchmaker.Matcher;
import rocks.matchmaker.Pattern;
import rocks.matchmaker.PatternVisitor;

public class TypeOfPattern<T> extends Pattern<T> {

    private final Class<T> expectedClass;

    public TypeOfPattern(Class<T> expectedClass) {
        this.expectedClass = expectedClass;
    }

    public Class<T> expectedClass() {
        return expectedClass;
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
