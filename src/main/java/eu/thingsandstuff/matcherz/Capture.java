package eu.thingsandstuff.matcherz;

public class Capture<T> {

    public static <T> Capture<T> newCapture() {
        return new Capture<>();
    }
}
