package rocks.matchmaker;

public interface MatchingStrategy {

    <T> Match<T> match(Matcher<T> matcher, Object object, Captures captures);
}
