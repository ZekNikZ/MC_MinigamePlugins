package io.zkz.mc.minigameplugins.battlebox;

public class Points {
    public static int getPlayerPlacementPointValue(int placement) {
        int base = 40 - 2 * placement;
        if (placement < 10) {
            base += 80 - 5 * placement;
        }
        return base;
    }

    public static int getTeamPlacementPointValue(int placement) {
        return Math.max(100 - 25 * placement, 0);
    }
}
