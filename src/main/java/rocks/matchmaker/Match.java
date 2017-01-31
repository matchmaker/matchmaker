package rocks.matchmaker;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

import static rocks.matchmaker.Util.checkNotNull;

public abstract class Match<T> {

    public abstract boolean isPresent();

    public abstract T value();

    public final boolean isEmpty() {
        return !isPresent();
    }

    static <S> Match<S> of(S value, Captures captures) {
        checkNotNull(captures);
        return new Match.Present<>(value, captures);
    }

    static <S> Match<S> empty() {
        return new Match.Empty<>();
    }

    public abstract Match<T> filter(Predicate<T> predicate);

    public abstract <U> Match<U> map(Function<? super T, ? extends U> mapper);

    public abstract <U> Match<U> flatMap(Function<? super T, Match<U>> mapper);

    public <S> S capture(Capture<S> capture) {
        return captures().get(capture);
    }

    public abstract Captures captures();

    static class Present<T> extends Match<T> {

        private final T value;
        private final Captures captures;

        private Present(T value, Captures captures) {
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
            return Match.of(mapper.apply(value), captures());
        }

        @Override
        public <U> Match<U> flatMap(Function<? super T, Match<U>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public Captures captures() {
            return captures;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Present<?> present = (Present<?>) o;

            if (value != null ? !value.equals(present.value) : present.value != null) return false;
            return captures.equals(present.captures);
        }

        @Override
        public int hashCode() {
            int result = value != null ? value.hashCode() : 0;
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
        public Captures captures() {
            throw new NoSuchElementException("Empty match contains no value");
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
