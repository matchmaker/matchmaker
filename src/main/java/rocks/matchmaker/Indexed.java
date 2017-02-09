package rocks.matchmaker;

public class Indexed<T> implements Comparable<Indexed<T>> {

    private final int index;
    private final T value;

    private Indexed(int index, T value) {
        Util.checkArgument(index >= 0, "Index can't be null");
        this.index = index;
        this.value = Util.checkNotNull(value);
    }

    public static <T> Indexed<T> at(int index, T value) {
        return new Indexed<>(index, value);
    }

    public int index() {
        return index;
    }

    public T value() {
        return value;
    }

    @Override
    public int compareTo(Indexed<T> o) {
        return index - o.index;
    }
}
