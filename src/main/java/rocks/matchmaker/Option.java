package rocks.matchmaker;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Option<T> {

    public abstract boolean isPresent();

    public abstract T value();

    static <S> Option<S> of(S value) {
        return new Present<>(value);
    }

    static <S> Option<S> empty() {
        return new Empty<>();
    }

    public abstract Option<T> filter(Predicate<T> predicate);

    public abstract <U> Option<U> map(Function<? super T, ? extends U> mapper);

    public abstract <U> Option<U> flatMap(Function<? super T, Option<U>> mapper);

    public T orElse(T fallback) {
        return isPresent() ? value() : fallback;
    }

    static class Present<T> extends Option<T> {

        private final T value;

        private Present(T value) {
            this.value = value;
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
        public Option<T> filter(Predicate<T> predicate) {
            return predicate.test(value) ? this : empty();
        }

        @Override
        public <U> Option<U> map(Function<? super T, ? extends U> mapper) {
            return Option.of(mapper.apply(value));
        }

        @Override
        public <U> Option<U> flatMap(Function<? super T, Option<U>> mapper) {
            return mapper.apply(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Present<?> present = (Present<?>) o;

            return value.equals(present.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public String toString() {
            return "Option.Present(" +
                    "value=" + value +
                    ')';
        }
    }

    static class Empty<T> extends Option<T> {

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public T value() {
            throw new NoSuchElementException("Empty Option contains no value");
        }

        @Override
        public Option<T> filter(Predicate<T> predicate) {
            return this;
        }

        @Override
        public <U> Option<U> map(Function<? super T, ? extends U> mapper) {
            return empty();
        }

        @Override
        public <U> Option<U> flatMap(Function<? super T, Option<U>> mapper) {
            return empty();
        }

        public boolean equals(Object o) {
            return this == o || (o != null && getClass() == o.getClass());
        }

        @Override
        public String toString() {
            return "Option.Empty()";
        }
    }
}
