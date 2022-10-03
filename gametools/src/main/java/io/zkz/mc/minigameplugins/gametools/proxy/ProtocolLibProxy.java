package io.zkz.mc.minigameplugins.gametools.proxy;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Objects;

public class ProtocolLibProxy {
    private ProtocolLibProxy() {
    }

    public static void setupGlowing(Plugin plugin) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (!TeamService.getInstance().isGlowingEnabled()) {
                    return;
                }

                PacketContainer packet = event.getPacket().deepClone();

                Player reciever = event.getPlayer();
                int receiverId = reciever.getEntityId();
                GameTeam recieverTeam = TeamService.getInstance().getTeamOfPlayer(reciever);

                int packetAboutId = packet.getIntegers().read(0);
                Player aboutPlayer = getPlayer(packetAboutId);

                if (receiverId == packetAboutId
                    || aboutPlayer == null
                    || aboutPlayer.getGameMode() == GameMode.SPECTATOR
                ) {
                    return;
                }

                GameTeam aboutPlayerTeam = TeamService.getInstance().getTeamOfPlayer(aboutPlayer);

                if (Objects.equals(aboutPlayerTeam, recieverTeam)) {
                    List<WrappedWatchableObject> watchableObjectList = packet.getWatchableCollectionModifier().read(0);
                    for (WrappedWatchableObject metadata : watchableObjectList) {
                        if (metadata.getIndex() == 0) {
                            byte b = (byte) metadata.getValue();
                            b |= 0b01000000;
                            metadata.setValue(b);
                        }
                    }
                }

                event.setPacket(packet);
            }
        });
    }

    private static Player getPlayer(final int entityId) {
        for (final Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.getEntityId() == entityId) return player;
        }
        return null;
    }
}
