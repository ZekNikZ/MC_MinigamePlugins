package io.zkz.mc.minigameplugins.minigamemanager.state;

public enum MinigameState {
    /**
     * Server is loading. Don't use.
     */
    LOADING("Loading"),
    /**
     * Minigame is currently setting itself up
     */
    SETUP("Setup"),
    /**
     * Waiting for all players to join the server
     */
    WAITING_FOR_PLAYERS("Waiting for players"),
    /**
     * Minigame is displaying the rules to players
     */
    RULES("Rules"),
    /**
     * Waiting to all players to be ready
     */
    WAITING_TO_BEGIN("Waiting to begin"),
    /**
     * Pre-round setup - after all players are ready but before round has started
     */
    PRE_ROUND("Starting"),
    /**
     * In-game - players are currently playing
     */
    IN_GAME("In game"),
    /**
     * Paused - game is paused, lock players in place, etc.
     */
    PAUSED("Paused"),
    /**
     * Post-round cleanup - round is over
     */
    POST_ROUND("Round over"),
    /**
     * Post-game cleanup - game is over
     */
    POST_GAME("Game over")
    ;

    private final String stateString;

    MinigameState(String stateString) {
        this.stateString = stateString;
    }

    @Override
    public String toString() {
        return this.stateString;
    }
}
