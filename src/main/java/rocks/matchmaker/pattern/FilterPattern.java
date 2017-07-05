package rocks.matchmaker.pattern;

import rocks.matchmaker.Captures;
import rocks.matchmaker.Match;
import rocks.matchmaker.Matcher;
import rocks.matchmaker.Pattern;

import java.util.function.Predicate;

public class FilterPattern<T> extends Pattern<T> {

    private final Predicate<? super T> predicate;

    public FilterPattern(Predicate<? super T> predicate, Pattern<T> previous) {
        super(previous);
        this.predicate = predicate;
    }

    public Predicate<? super T> predicate() {
        return predicate;
    }

    @Override
    public Match<T> accept(Matcher matcher, Object object, Captures captures) {
        return matcher.visit(this, object, captures);
    }
}
