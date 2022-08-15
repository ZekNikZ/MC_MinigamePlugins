package io.zkz.mc.minigameplugins.lobby;

import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.SubServers.Client.Bukkit.Event.SubStartedEvent;
import net.ME1312.SubServers.Client.Bukkit.Event.SubStoppedEvent;
import net.ME1312.SubServers.Client.Bukkit.SubAPI;
import net.ME1312.SubServers.Client.Common.Network.API.SubCreator;
import net.ME1312.SubServers.Client.Common.Network.API.SubServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class TournamentManager extends PluginService<LobbyPlugin> {
    private static final TournamentManager INSTANCE = new TournamentManager();

    public static TournamentManager getInstance() {
        return INSTANCE;
    }

    private String currentMinigameId = null;

    public void createAndStartServer(String templateId) {
        // Note: this assumes only one host
        SubAPI.getInstance().getHosts(hosts -> {
            SubCreator creator = hosts.get("~").getCreator();
            SubCreator.ServerTemplate template = creator.getTemplate(templateId);
            creator.create(templateId, template, Version.fromString("1.19.2"), null, i -> currentMinigameId = templateId);
        });
    }

    public void sendPlayersToServer(String minigameId) {
        SubAPI.getInstance().getRemotePlayers(players -> {
            players.forEach((playerId, player) -> {
                player.transfer(minigameId);
            });
        });
    }

    private void removeServer(String minigameId) {
        SubAPI.getInstance().getSubServer(minigameId, SubServer::stop);
    }

    public void startMinigame(String minigameId) {
        this.currentMinigameId = minigameId;
        this.createAndStartServer(this.currentMinigameId);
    }

    public void resetMinigame() {
        String minigameId = this.currentMinigameId;
        this.currentMinigameId = null;
        this.removeServer(minigameId);
    }

    @EventHandler
    private void onSubServerStart(SubStartedEvent event) {
        if (this.currentMinigameId != null && event.getServer().equals(this.currentMinigameId)) {
            this.sendPlayersToServer(this.currentMinigameId);
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        if (this.currentMinigameId != null) {
            Chat.sendAlert(event.getPlayer(), ChatType.ACTIVE_INFO, "A minigame is currently in progress. Sending you to that server in 10 seconds...");
            BukkitUtils.runLater(() -> {
                SubAPI.getInstance().getRemotePlayer(event.getPlayer().getUniqueId(), rp -> {
                    rp.transfer(this.currentMinigameId);
                });
            }, 200);
        }
    }
}
