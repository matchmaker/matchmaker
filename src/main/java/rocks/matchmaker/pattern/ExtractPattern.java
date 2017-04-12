package rocks.matchmaker.pattern;

import rocks.matchmaker.Extractor;
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
}
