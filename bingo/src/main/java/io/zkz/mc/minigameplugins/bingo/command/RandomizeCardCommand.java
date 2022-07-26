package io.zkz.mc.minigameplugins.bingo.command;

import io.zkz.mc.minigameplugins.bingo.BingoRound;
import io.zkz.mc.minigameplugins.bingo.Permissions;
import io.zkz.mc.minigameplugins.bingo.map.BingoCardMap;
import io.zkz.mc.minigameplugins.gametools.command.ArgumentCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = RandomizeCardCommand.COMMAND_NAME,
    desc = "Randomize the card of the current round",
    usage = "/" + RandomizeCardCommand.COMMAND_NAME,
    permission = Permissions.Card.RANDOMIZE
))
@org.bukkit.plugin.java.annotation.permission.Permissions(
    @Permission(name = Permissions.Card.RANDOMIZE, desc = "Randomize bingo card")
)
public class RandomizeCardCommand extends ArgumentCommandExecutor {
    static final String COMMAND_NAME = "randomizecard";

    protected RandomizeCardCommand() {
        super(COMMAND_NAME, 0);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        ((BingoRound) MinigameService.getInstance().getCurrentRound()).randomizeCard();
        BukkitUtils.runNextTick(BingoCardMap::markDirty);

        return true;
    }
}
