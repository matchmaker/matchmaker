package rocks.matchmaker.pattern;

import rocks.matchmaker.Pattern;

public class CombinePattern<T> extends Pattern<T> {

    private final Pattern<T> pattern;

    public CombinePattern(Pattern<?> previous, Pattern<T> pattern) {
        super(previous);
        this.pattern = pattern;
    }

    public Pattern<T> pattern() {
        return pattern;
    }
}
