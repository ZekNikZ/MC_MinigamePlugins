package io.zkz.mc.minigameplugins.bingo;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Points {
    public static final Map<Integer, String> ORDINALS = Map.of(
        45, "1st",
        30, "2nd",
        20, "3rd",
        10, "4th"
    );
    public static final List<Integer> POINT_VALUES = ORDINALS.keySet().stream().sorted(Comparator.reverseOrder()).toList();
    public static final int INITIAL_POINTS = POINT_VALUES.get(0);
}
