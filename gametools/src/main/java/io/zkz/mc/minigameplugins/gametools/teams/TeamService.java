package io.zkz.mc.minigameplugins.gametools.teams;

import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.UUID;

public class TeamService extends GameToolsService {
    private static final TeamService INSTANCE = new TeamService();

    public static TeamService getInstance() {
        return INSTANCE;
    }

    @Override
    protected void setup() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        GameTeam team = this.getTeamOfPlayer(event.getPlayer());
        if (team != null) {
            event.setFormat("<" + team.getFormatCode() + ChatColor.BOLD + team.getPrefix() + ChatColor.RESET + team.getFormatCode() + " %1$s" + ChatColor.RESET + "> %2$s");
        }
    }

    public void createTeam(GameTeam team) {
        // TODO: implement
    }

    public void registerDefaultTeams() {
        // TODO: implement
        this.clearTeams();

        DefaultTeams.addAll();
    }

    public void removeTeam(String id) {
        // TODO: implement
    }

    public void getTeam(String id) {
        // TODO: implement
    }

    public List<GameTeam> getAllTeams() {
        // TODO: implement
        return null;
    }

    public void clearTeams() {
        // TODO: implement
    }

    public void joinTeam(Player player, GameTeam team) {
        this.joinTeam(player.getUniqueId(), team.getId());
    }

    public void joinTeam(UUID playerId, String teamId) {
        // TODO: implement
    }

    public void leaveTeam(Player player) {
        this.leaveTeam(player.getUniqueId());
    }

    public void leaveTeam(UUID playerId) {
        // TODO: implement
    }

    public GameTeam getTeamOfPlayer(Player player) {
        return this.getTeamOfPlayer(player.getUniqueId());
    }

    public GameTeam getTeamOfPlayer(UUID uniqueId) {
        // TODO: implement
        return null;
    }

    public List<UUID> getTeamMembers(String teamId) {
        // TODO: implement
        return null;
    }

    public List<UUID> getTeamMembers(GameTeam team) {
        return this.getTeamMembers(team.getId());
    }

    public List<Player> getOnlineTeamMembers(String teamId) {
        // TODO: implement
        return null;
    }

    public void clearTeam(String teamId) {
        // TODO: implement
    }
}
