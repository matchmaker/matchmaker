package rocks.matchmaker.pattern;

import rocks.matchmaker.Captures;
import rocks.matchmaker.Match;
import rocks.matchmaker.Matcher;
import rocks.matchmaker.Pattern;
import rocks.matchmaker.PatternVisitor;

public class CombinePattern<T> extends Pattern<T> {

    private final Pattern<T> pattern;

    public CombinePattern(Pattern<?> previous, Pattern<T> pattern) {
        super(previous);
        this.pattern = pattern;
    }

    public Pattern<T> pattern() {
        return pattern;
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
