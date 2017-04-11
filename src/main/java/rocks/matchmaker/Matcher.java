package rocks.matchmaker;

public interface Matcher {

    <T> Match<T> match(Pattern<T> pattern, Object object);
}
