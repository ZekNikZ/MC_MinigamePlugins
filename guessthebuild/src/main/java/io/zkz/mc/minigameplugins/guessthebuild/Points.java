package io.zkz.mc.minigameplugins.guessthebuild;

public class Points {
    public static int SUCCESSFUL_BUILD = 50;

    public static int getPlayerPlacementPointValue(int placement) {
        int base = 30 - 2 * placement;
        if (placement < 5) {
            base += 30 - 5 * placement;
        }
        return base;
    }

    public static int getTeamPlacementPointValue(int placement) {
        return Math.max(100 - 25 * placement, 0);
    }
}
