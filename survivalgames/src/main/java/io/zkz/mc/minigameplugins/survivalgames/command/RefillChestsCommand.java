package io.zkz.mc.minigameplugins.survivalgames.command;

import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.sound.SoundUtils;
import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.survivalgames.Permissions;
import io.zkz.mc.minigameplugins.survivalgames.SGService;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = RefillChestsCommand.COMMAND_NAME,
    desc = "Refill chests",
    usage = "/" + RefillChestsCommand.COMMAND_NAME,
    permission = Permissions.Event.REFILL_CHESTS
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.Event.REFILL_CHESTS,
    desc = "Refill chests"
))
public class RefillChestsCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "refillchests";

    protected RefillChestsCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        SGService.getInstance().getCurrentRound().refillChests();

        return true;
    }
}
