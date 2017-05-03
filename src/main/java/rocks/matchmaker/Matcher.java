package rocks.matchmaker;

public interface Matcher {

    default <T> Match<T> match(Pattern<T> pattern, Object object) {
        return match(pattern, object, Captures.empty());
    }

    <T> Match<T> match(Pattern<T> pattern, Object object, Captures captures);
}
