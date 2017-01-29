package eu.thingsandstuff.matcherz;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

abstract class Match<T> {

    public abstract boolean isPresent();

    public abstract T value();

    public final boolean isEmpty() {
        return !isPresent();
    }

    static <S> Match<S> of(S value) {
        return new Match.Present<>(value, emptyMap());
    }

    static <S> Match<S> of(S value, Map<Capture<?>, Object> captures) {
        return new Match.Present<>(value, immutableMap(captures));
    }

    static Map<Capture<?>, Object> immutableMap(Map<Capture<?>, Object> captures) {
        return unmodifiableMap(new LinkedHashMap<>(captures));
    }

    static <S> Match<S> empty() {
        return new Match.Empty<>();
    }

    public abstract Match<T> filter(Predicate<T> predicate);

    public abstract <U> Match<U> map(Function<? super T, ? extends U> mapper);

    public abstract <U> Match<U> flatMap(Function<? super T, Match<U>> mapper);

    public abstract <S> S capture(Capture<S> vowels);

    protected abstract Map<Capture<?>, Object> captures();

    protected abstract Match<T> withCapture(Capture<?> capture, Object value);

    static class Present<T> extends Match<T> {

        private final T value;
        private final Map<Capture<?>, Object> captures;

        private Present(T value, Map<Capture<?>, Object> captures) {
            this.value = value;
            this.captures = captures;
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public T value() {
            return value;
        }

        @Override
        public Match<T> filter(Predicate<T> predicate) {
            return predicate.test(value) ? this : empty();
        }

        @Override
        public <U> Match<U> map(Function<? super T, ? extends U> mapper) {
            return Match.of(mapper.apply(value), captures);
        }

        @Override
        public <U> Match<U> flatMap(Function<? super T, Match<U>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public <S> S capture(Capture<S> capture) {
            if (!captures.containsKey(capture)) {
                throw new IllegalArgumentException("This capture is unknown to this matcher.");
            }
            return getCaptureAndCast(capture);
        }

        @Override
        protected Map<Capture<?>, Object> captures() {
            return captures;
        }

        @Override
        public Match<T> withCapture(Capture<?> capture, Object value) {
            LinkedHashMap<Capture<?>, Object> newCaptures = new LinkedHashMap<>(captures);
            newCaptures.put(capture, value);
            return Match.of(this.value, newCaptures);
        }

        @SuppressWarnings("unchecked cast")
        private <S> S getCaptureAndCast(Capture<S> capture) {
            return (S) captures.get(capture);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Present<?> present = (Present<?>) o;

            if (!value.equals(present.value)) return false;
            return captures.equals(present.captures);
        }

        @Override
        public int hashCode() {
            int result = value.hashCode();
            result = 31 * result + captures.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Match.Present(" +
                    "value=" + value +
                    ", captures=" + captures +
                    ')';
        }
    }

    static class Empty<T> extends Match<T> {

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public T value() {
            throw new NoSuchElementException("Empty match contains no value");
        }

        @Override
        public Match<T> filter(Predicate<T> predicate) {
            return this;
        }

        @Override
        public <U> Match<U> map(Function<? super T, ? extends U> mapper) {
            return empty();
        }

        @Override
        public <U> Match<U> flatMap(Function<? super T, Match<U>> mapper) {
            return empty();
        }

        @Override
        public <S> S capture(Capture<S> vowels) {
            throw new NoSuchElementException("Captures are undefined for an empty match");
        }

        @Override
        protected Map<Capture<?>, Object> captures() {
            throw new NoSuchElementException("Captures are undefined for an empty match");
        }

        @Override
        protected Match<T> withCapture(Capture<?> capture, Object value) {
            throw new IllegalStateException("Can't register capture in an empty match");
        }

        public boolean equals(Object o) {
            return this == o || (o != null && getClass() == o.getClass());
        }

        @Override
        public String toString() {
            return "Match.Empty()";
        }
    }
}
