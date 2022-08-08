package io.zkz.mc.minigameplugins.gametools.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListUtils {
    @SafeVarargs
    public static <T> List<T> of(T... items) {
        return Arrays.asList(items);
    }

    public static <T> List<T> of(List<T> items) {
        return new ArrayList<>(items);
    }

    public static <T> List<T> ofImmutable(List<T> items) {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }
}
