package rocks.matchmaker;

import rocks.matchmaker.pattern.EqualsPattern;
import rocks.matchmaker.pattern.TypeOfPattern;

public class DefaultMatcher implements Matcher {

    public static final Matcher DEFAULT_MATCHER = new DefaultMatcher();

    @Override
    public <T> Match<T> match(Pattern<T> pattern, Object object) {
        return match(pattern, object, Captures.empty());
    }

    @Override
    public <T> Match<T> match(Pattern<T> pattern, Object object, Captures captures) {
        if (pattern instanceof EqualsPattern) {
            return Match.of((T) object, captures).filter(o -> ((EqualsPattern) pattern).expectedValue().equals(object));
        } else if (pattern instanceof TypeOfPattern) {
            return Match.of((T) object, captures).filter(o -> ((TypeOfPattern) pattern).expectedClass().isInstance(object));
        } else {
            return pattern.match(object, captures);
        }
    }
}
