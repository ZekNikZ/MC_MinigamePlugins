package io.zkz.mc.minigameplugins.minigamemanager.minigame;

import io.zkz.mc.minigameplugins.gametools.scoreboard.GameScoreboard;
import io.zkz.mc.minigameplugins.gametools.teams.DefaultTeams;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.DefaultScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.scoreboard.MinigameScoreboard;
import io.zkz.mc.minigameplugins.minigamemanager.state.IPlayerState;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Class that represents an instance of a minigame. This class defines all the metadata and hooks of a minigame and is
 * also responsible for constructing the minigame rounds and registering the metadata with the minigame manager.
 *
 * @param <R> the type of the rounds associated with the minigame
 * @see MinigameService#setMinigame(Minigame)
 */
public abstract class Minigame<R extends Round> {
    // region Abstract Methods

    /**
     * Name of the minigame.
     *
     * @return the name of the minigame, for use in chat messages
     */
    public abstract String getMinigameName();

    /**
     * Build the list of rules slides to be displayed in the pre-game phase.
     *
     * @return a list of "slides" (= lists of components) each component representing a single line in the chat
     */
    public abstract @NotNull List<List<Component>> buildRulesSlides();

    /**
     * Build the list of rounds in the minigame.
     *
     * @return the list of rounds in the game
     */
    public abstract @NotNull List<? extends R> buildRounds();

    /**
     * Callback to formally end the minigame. Called when the minigame reaches it's post-game state.
     */
    // TODO: make a TournamentMinigame subclass (as part of Tournament Connector) which handles this properly
    public abstract void handleMinigameOver();
    // endregion

    // region Overridable Defaults

    /**
     * Length (in ticks) of the pre-round phase.
     *
     * @return number of ticks
     */
    public int getPreRoundDelay() {
        return 200;
    }

    /**
     * Length (in ticks) of the post-round phase.
     *
     * @return number of ticks
     */
    public int getPostRoundDelay() {
        return 200;
    }

    /**
     * Length (in ticks) of the post-game phase.
     *
     * @return number of ticks
     */
    public int getPostGameDelay() {
        return 200;
    }

    /**
     * Whether the rules will automatically be shown when all participants are online.
     *
     * @return if true, the rules will automatically be shown
     */
    public boolean getAutomaticShowRules() {
        return false;
    }

    /**
     * Whether players will be asked to ready on a per-round basis or a per-game basis.
     *
     * @return if true, players will ready up each round; if false, only at the beginning of the game
     */
    public boolean getReadyUpEachRound() {
        return false;
    }

    /**
     * Whether the game will automatically go to the next round after a round finishes.
     *
     * @return if true, the game will automatically go to the next round
     */
    public boolean getAutomaticNextRound() {
        return true;
    }

    public boolean getShowScoreSummary() {
        return true;
    }

    /**
     * Get the list of participating players.
     *
     * @return the list of player UUIDs that are participating in the minigame
     * @apiNote override this if not all non-spectators are participating
     */
    public Collection<UUID> getParticipants() {
        return Stream.concat(
                TeamService.getInstance().getTrackedPlayers().stream(),
                Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId)
            )
            .distinct()
            .filter(uuid -> {
                GameTeam team = TeamService.getInstance().getTeamOfPlayer(uuid);
                if (this.isTeamGame()) {
                    return team != null && !team.spectator();
                } else {
                    return team == null || !team.spectator();
                }
            })
            .toList();
    }

    /**
     * Get the list of participating players and game masters.
     *
     * @return the list of player UUIDs that are participating in the minigame or a game master
     * @apiNote override this if not all non-spectators are participating
     */
    public Collection<UUID> getParticipantsAndGameMasters() {
        return Stream.concat(
                TeamService.getInstance().getTrackedPlayers().stream(),
                Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId)
            )
            .distinct()
            .filter(uuid -> {
                GameTeam team = TeamService.getInstance().getTeamOfPlayer(uuid);
                if (this.isTeamGame()) {
                    return team != null && (DefaultTeams.GAME_MASTER.equals(team) || !team.spectator());
                } else {
                    return team == null || DefaultTeams.GAME_MASTER.equals(team) || !team.spectator();
                }
            })
            .toList();
    }

    /**
     * Determines whether the game is a team game or not.
     *
     * @return whether the game is a team game
     * @apiNote override this if you want to disable checking if all players are on a team
     */
    public boolean isTeamGame() {
        return true;
    }

    /**
     * Handler for state changes. Run when a new state is being set up.
     *
     * @param state the new state
     * @apiNote override this if you need to run some code when a state changes
     */
    public void onStateSetup(MinigameState state) {

    }

    /**
     * Handler for state changes. Run when an old state is being cleaned up.
     *
     * @param state the old state
     * @apiNote override this if you need to run some code when a state changes
     */
    public void onStateCleanup(MinigameState state) {

    }

    /**
     * Create the tasks that should run for a given minigame state.
     *
     * @param state the new state
     * @apiNote override this if you need to run task(s) during a state
     */
    public @NotNull List<Supplier<? extends MinigameTask>> buildTasks(MinigameState state) {
        return List.of();
    }

    /**
     * Build the player state that will be used to set common player properties.
     *
     * @return a map of minigame state to player state
     */
    public @Nullable IPlayerState buildPlayerState(MinigameState state) {
        return null;
    }

    /**
     * Provides a {@link MinigameScoreboard} for a state.
     *
     * @param state the current state
     * @return a {@link MinigameScoreboard} to be displayed to the players on the server
     * @apiNote override this if you wish to not use the default scoreboard (use super() to use the default scoreboard)
     */
    public @Nullable MinigameScoreboard buildScoreboard(MinigameState state) {
        return DefaultScoreboard.DEFAULT_SCOREBOARD;
    }

    /**
     * Modifies the default scoreboard. Note: only used to modify the default scoreboard.
     *
     * @param state      the current state
     * @param scoreboard the scoreboard to modify
     * @apiNote override this if you only want to make small adjustments to the default scoreboard
     */
    public void modifyScoreboard(MinigameState state, GameScoreboard scoreboard) {

    }

    // endregion

    // region Proxy Methods

    /**
     * Get the current round of the minigame.
     *
     * @return the current round of the minigame
     */
    public final R getCurrentRound() {
        return MinigameService.getInstance().getCurrentRound();
    }

    // endregion
}
