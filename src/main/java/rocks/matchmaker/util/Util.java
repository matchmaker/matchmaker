package rocks.matchmaker.util;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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

    public static Stream<Class<?>> supertypes(Class<?> type) {
        return TypeToken.of(type).getTypes().stream().map(TypeToken::getRawType);
    }

    public static void checkArgument(boolean expression, String message) {
        Preconditions.checkArgument(expression, message);
    }
}
