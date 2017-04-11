package rocks.matchmaker;

public class ExtractPattern<T, R> extends Pattern<R> {

    private final Extractor<T, R> extractor;

    public ExtractPattern(Extractor<T, R> extractor) {
        this.extractor = extractor;
    }

    public Extractor<T, R> extractor() {
        return extractor;
    }
}
