package io.zkz.mc.minigameplugins.minigamemanager.proxy;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import io.zkz.mc.minigameplugins.gametools.teams.DefaultTeams;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Objects;

public class ProtocolLibProxy {
    public static void setupGlowing(Plugin plugin) {
        return; // disable for now

//        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
//        protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA /*, PacketType.Play.Server.NAMED_ENTITY_SPAWN */) {
//            @Override
//            public void onPacketSending(PacketEvent event) {
//                if (!MinigameService.getInstance().isGlowingEnabled()) {
//                    return;
//                }
//
//                GameTeam playerTeam = TeamService.getInstance().getTeamOfPlayer(event.getPlayer());
//
//                for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
//                    GameTeam otherPlayerTeam = TeamService.getInstance().getTeamOfPlayer(otherPlayer);
//                    if (otherPlayer.getEntityId() == event.getPacket().getIntegers().read(0)) {
//                        if (otherPlayer.getGameMode() != GameMode.SPECTATOR && (Objects.equals(otherPlayerTeam, playerTeam) || (playerTeam != null && playerTeam.isSpectator()))) {
//                            if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
//                                List<WrappedWatchableObject> watchableObjectList = event.getPacket().getWatchableCollectionModifier().read(0);
//                                for (WrappedWatchableObject metadata : watchableObjectList) {
//                                    if (metadata.getIndex() == 0) {
//                                        byte b = (byte) metadata.getValue();
//                                        b |= 0b01000000;
//                                        metadata.setValue(b);
//                                    }
//                                }
//                            }
//                            /* else {
//                                WrappedDataWatcher watcher = event.getPacket().getDataWatcherModifier().read(0);
//                                if (watcher.hasIndex(0)) {
//                                    byte b = watcher.getByte(0);
//                                    b |= 0b01000000;
//                                    watcher.setObject(0, b);
//                                }
//                            } */
//                        }
//                    }
//                }
//            }
//        });
    }
}
