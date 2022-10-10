package io.zkz.mc.uhc;

public enum GameState {
    UNKNOWN("Unknown", false, false),
    SETUP("Setup", false, false),
    PRE_GAME("Pre-game", false, false),
    WB_CLOSING("In-game", true, false),
    WB_STOPPED("In-game", true, false),
    SUDDEN_DEATH("Sudden Death", true, false),
    PAUSED("Game Paused", true, true),
    POST_GAME("Post-game", false, false),
    ;

    private final String commonName;
    private final boolean inGame;
    private final boolean paused;

    GameState(String commonName, boolean inGame, boolean paused) {
        this.commonName = commonName;
        this.inGame = inGame;
        this.paused = paused;
    }

    public String getCommonName() {
        return commonName;
    }

    public boolean isInGame() {
        return inGame;
    }

    public boolean isPaused() {
        return paused;
    }
}
