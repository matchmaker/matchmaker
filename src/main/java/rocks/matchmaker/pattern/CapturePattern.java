package rocks.matchmaker.pattern;

import rocks.matchmaker.Capture;
import rocks.matchmaker.Captures;
import rocks.matchmaker.Match;
import rocks.matchmaker.Matcher;
import rocks.matchmaker.Pattern;

public class CapturePattern<T> extends Pattern<T> {

    private final Capture<T> capture;

    public CapturePattern(Capture<T> capture, Pattern<T> previous) {
        super(previous);
        this.capture = capture;
    }

    public Capture<T> capture() {
        return capture;
    }

    @Override
    public Match<T> accept(Matcher matcher, Object object, Captures captures) {
        return matcher.visit(this, object, captures);
    }
}
