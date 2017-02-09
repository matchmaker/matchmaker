package rocks.matchmaker;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Util {

    public static <T> List<T> append(List<T> list, T element) {
        ArrayList<T> newList = new ArrayList<>(list);
        newList.add(element);
        return Collections.unmodifiableList(newList);
    }

    public static <T> T checkNotNull(T value) {
        if (value == null) {
            throw new NullPointerException("Value cannot be null");
        }
        return value;
    }

    public static void checkArgument(boolean expression, String message) {
        Preconditions.checkArgument(expression, message);
    }
}
