package io.zkz.mc.minigameplugins.lobby;

import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.SubServers.Client.Bukkit.SubAPI;
import net.ME1312.SubServers.Client.Common.Network.API.SubCreator;

public class TournamentManager extends PluginService<LobbyPlugin> {
    private static final TournamentManager INSTANCE = new TournamentManager();

    public static TournamentManager getInstance() {
        return INSTANCE;
    }

    public void startMinigameServer(String templateId, Runnable callbackWhenStarted) {
        // Note: this assumes only one host
        SubAPI.getInstance().getHosts(hosts -> {
            SubCreator creator =  hosts.get("~").getCreator();
            SubCreator.ServerTemplate template = creator.getTemplate(templateId);
            creator.create(templateId, template, Version.fromString("1.19.2"), null, i -> callbackWhenStarted.run());
        });
    }

    public void sendPlayersToServer(String minigameId) {
        SubAPI.getInstance().getSubServer("lobby", subServer -> {
            subServer.getRemotePlayers(players -> {
                players.forEach(player -> player.transfer(minigameId));
            });
        });
    }
}
