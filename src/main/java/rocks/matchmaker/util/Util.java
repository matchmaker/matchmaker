package rocks.matchmaker.util;

import com.google.common.base.Preconditions;

public class Util {

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
