package eu.thingsandstuff.matcherz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Util {

    public static <T> List<T> append(List<T> list, T element) {
        ArrayList<T> newList = new ArrayList<>(list);
        newList.add(element);
        return Collections.unmodifiableList(newList);
    }
}
