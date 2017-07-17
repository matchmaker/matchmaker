package rocks.matchmaker;

import rocks.matchmaker.pattern.CapturePattern;
import rocks.matchmaker.pattern.CombinePattern;
import rocks.matchmaker.pattern.EqualsPattern;
import rocks.matchmaker.pattern.ExtractPattern;
import rocks.matchmaker.pattern.FilterPattern;
import rocks.matchmaker.pattern.TypeOfPattern;
import rocks.matchmaker.pattern.WithPattern;
import rocks.matchmaker.util.Util;

public class DefaultPrinter implements PatternVisitor<String> {

    private StringBuilder result = new StringBuilder();
    private int level = 0;

    @Override
    public String result() {
        return result.toString();
    }

    @Override
    public void visit(CapturePattern<?> pattern) {
        visitPrevious(pattern);
        appendLine("capturedAs(@%s)", pattern.capture().sequenceNumber());
    }

    @Override
    public void visit(CombinePattern<?> pattern) {
        visitPrevious(pattern);
        pattern.pattern().accept(this);
    }

    @Override
    public void visit(EqualsPattern<?> pattern) {
        visitPrevious(pattern);
        appendLine("equalTo(%s)", pattern.expectedValue());
    }

    @Override
    public void visit(ExtractPattern<?, ?> pattern) {
        visitPrevious(pattern);
        appendLine("extract(%s)", pattern.extractor());
    }

    @Override
    public void visit(FilterPattern<?> pattern) {
        visitPrevious(pattern);
        appendLine("filter(%s)", pattern.predicate());
    }

    @Override
    public void visit(TypeOfPattern<?> pattern) {
        visitPrevious(pattern);
        appendLine("typeOf(%s)", pattern.expectedClass().getSimpleName());
    }

    @Override
    public void visit(WithPattern<?> pattern) {
        visitPrevious(pattern);
        appendLine("with(%s)", pattern.getProperty()); //TODO provide actual name
        level += 1;
        pattern.getPattern().accept(this);
        level -= 1;
    }

    @Override
    public void visitPattern(Pattern<?> pattern) {
        appendLine(pattern.toString());
    }

    private void appendLine(String template, Object... arguments) {
        result.append(Util.indent(level, template + "\n", arguments));
    }
}
