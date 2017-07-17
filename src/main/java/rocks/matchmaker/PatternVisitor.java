package rocks.matchmaker;

import rocks.matchmaker.pattern.CapturePattern;
import rocks.matchmaker.pattern.CombinePattern;
import rocks.matchmaker.pattern.EqualsPattern;
import rocks.matchmaker.pattern.ExtractPattern;
import rocks.matchmaker.pattern.FilterPattern;
import rocks.matchmaker.pattern.TypeOfPattern;
import rocks.matchmaker.pattern.WithPattern;

public interface PatternVisitor<R> {

    R result();

    void visit(CapturePattern<?> pattern);

    void visit(CombinePattern<?> pattern);

    void visit(EqualsPattern<?> pattern);

    void visit(ExtractPattern<?, ?> pattern);

    void visit(FilterPattern<?> pattern);

    void visit(TypeOfPattern<?> pattern);

    void visit(WithPattern<?> pattern);

    default void visitPattern(Pattern<?> pattern) {
        throw new UnsupportedOperationException("Unsupported pattern type: " + pattern.getClass().getCanonicalName());
    }

    default void visitPrevious(Pattern pattern) {
        Pattern previous = pattern.previous();
        if (previous != null) {
            previous.accept(this);
        }
    }
}
