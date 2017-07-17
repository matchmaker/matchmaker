package rocks.matchmaker;

import java.util.concurrent.atomic.AtomicInteger;

public class Capture<T> {

    private static final AtomicInteger sequenceCounter = new AtomicInteger();

    private final int sequenceNumber;

    public static <T> Capture<T> newCapture() {
        return new Capture<>();
    }

    private Capture() {
        this.sequenceNumber = sequenceCounter.incrementAndGet();
    }

    public int sequenceNumber() {
        return sequenceNumber;
    }
}
