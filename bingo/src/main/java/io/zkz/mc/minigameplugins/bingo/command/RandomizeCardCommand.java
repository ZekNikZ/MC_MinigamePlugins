package io.zkz.mc.minigameplugins.bingo.command;

import io.zkz.mc.minigameplugins.bingo.BingoRound;
import io.zkz.mc.minigameplugins.bingo.map.BingoCardMap;
import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.minigamemanager.command.MinigameCommands;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.state.MinigameState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = RandomizeCardCommand.COMMAND_NAME,
    desc = "Randomize the card of the current round",
    usage = "/" + RandomizeCardCommand.COMMAND_NAME,
    permission = BingoCommands.Permissions.RANDOMIZE_CARD
))
@Permissions(
    @Permission(name = BingoCommands.Permissions.RANDOMIZE_CARD, desc = "Randomize bingo card")
)
public class RandomizeCardCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "randomizecard";

    protected RandomizeCardCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        ((BingoRound) MinigameService.getInstance().getCurrentRound()).getCard().randomizeItems();
        BukkitUtils.runNextTick(BingoCardMap::markDirty);

        return true;
    }
}
