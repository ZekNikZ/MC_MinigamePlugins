package io.zkz.mc.minigameplugins.uhc.task;

import io.zkz.mc.minigameplugins.gametools.util.ActionBarService;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import org.bukkit.entity.Player;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mmArgs;

public class CoordinatesHudTask extends MinigameTask {
    public CoordinatesHudTask() {
        super(1, 1);
    }

    @Override
    public void run() {
        BukkitUtils.forEachPlayer(player -> {
            ActionBarService.getInstance().addMessage(
                player.getUniqueId(),
                "coordshud",
                mmArgs(
                    "<legacy_gold>XYZ:</legacy_gold> <0> <1> <2>   <legacy_gold><3> (<4>)</legacy_gold>",
                    player.getLocation().getBlockX(),
                    player.getLocation().getBlockY(),
                    player.getLocation().getBlockZ(),
                    getCardinalDirection(player),
                    getBlockDirection(player)
                )
            );
        });
    }

    public static String getCardinalDirection(Player player) {
        double rotation = (player.getLocation().getYaw() - 180) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 45.0) {
            return "N";
        } else if (45.0 <= rotation && rotation < 135.0) {
            return "E";
        } else if (135.0 <= rotation && rotation < 225.0) {
            return "S";
        } else if (225.0 <= rotation && rotation < 315.0) {
            return "W";
        } else if (315.0 <= rotation && rotation < 360.0) {
            return "N";
        } else {
            return null;
        }
    }

    public static String getBlockDirection(Player player) {
        double rotation = (player.getLocation().getYaw() - 180) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 45.0) {
            return "-Z";
        } else if (45.0 <= rotation && rotation < 135.0) {
            return "+X";
        } else if (135.0 <= rotation && rotation < 225.0) {
            return "+Z";
        } else if (225.0 <= rotation && rotation < 315.0) {
            return "-X";
        } else if (315.0 <= rotation && rotation < 360.0) {
            return "-Z";
        } else {
            return null;
        }
    }
}
