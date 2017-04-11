package rocks.matchmaker;

public class DefaultMatcher implements Matcher {

    @Override
    public <T> Match<T> match(Pattern<T> pattern, Object object) {
        return pattern.match(object);
    }
}
