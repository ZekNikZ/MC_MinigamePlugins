package io.zkz.mc.minigameplugins.tgttos;

public class Points {
    public static int getPlayerPlacementPointValue(int placement) {
        return Math.max(50 - 3 * placement, 0);
    }

    public static int getTeamPlacementPointValue(int placement) {
        return Math.max(80 - 12 * placement, 0);
    }
}
