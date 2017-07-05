package rocks.matchmaker.pattern;

import rocks.matchmaker.Captures;
import rocks.matchmaker.Extractor;
import rocks.matchmaker.Match;
import rocks.matchmaker.Matcher;
import rocks.matchmaker.Pattern;

public class ExtractPattern<T, R> extends Pattern<R> {

    private final Extractor<T, R> extractor;

    public ExtractPattern(Extractor<T, R> extractor, Pattern<T> pattern) {
        super(pattern);
        this.extractor = extractor;
    }

    public Extractor<T, R> extractor() {
        return extractor;
    }

    @Override
    public Match<R> accept(Matcher matcher, Object object, Captures captures) {
        return matcher.visit(this, object, captures);
    }
}
