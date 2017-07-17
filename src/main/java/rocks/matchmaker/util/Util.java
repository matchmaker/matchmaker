package rocks.matchmaker.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ObjectArrays;

import static java.lang.String.format;
import static java.util.Collections.nCopies;

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

    public static String indent(int indentLevel, String template, Object... args) {
        Object[] newArgs = ObjectArrays.concat(padding(indentLevel), args);
        return format("%s" + template, newArgs);
    }

    private static String padding(int level) {
        return String.join("", nCopies(level, "\t"));
    }
}
