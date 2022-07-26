package io.zkz.mc.minigameplugins.dev.survivalgames;

import com.sk89q.worldedit.math.BlockVector3;
import io.zkz.mc.minigameplugins.dev.Permissions;
import io.zkz.mc.minigameplugins.gametools.command.AbstractCommandExecutor;
import io.zkz.mc.minigameplugins.gametools.worldedit.WorldEditService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.Permission;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;

@Commands(@org.bukkit.plugin.java.annotation.command.Command(
    name = SGDevCommand.COMMAND_NAME,
    desc = "Dev setup for SG",
    usage = "/" + SGDevCommand.COMMAND_NAME + " <subcommand>",
    permission = Permissions.SurvivalGames.ADMIN
))
@org.bukkit.plugin.java.annotation.permission.Permissions(@Permission(
    name = Permissions.SurvivalGames.ADMIN,
    desc = "Admin configuration for survival games"
))
public class SGDevCommand extends AbstractCommandExecutor {
    static final String COMMAND_NAME = "sg";

    static final Map<String, BiConsumer<CommandSender, String[]>> SUB_COMMANDS = Map.of(
        "createfinalarena", (sender, args) -> {
            if (args.length != 1) {
                sender.sendMessage("Usage: /sg createfinalarena <name>");
                return;
            }

            SGService.getInstance().createFinalArena(args[0]);
            sender.sendMessage("Created final arena " + args[0]);
        },
        "removefinalarena", (sender, args) -> {
            if (args.length != 1) {
                sender.sendMessage("Usage: /sg removefinalarena <name>");
                return;
            }

            SGService.getInstance().removeFinalArena(args[0]);
            sender.sendMessage("Removed final arena " + args[0]);
        },
        "listfinalarenas", (sender, args) -> {
            SGService.getInstance().listFinalArenas(sender);
        },
        "setfinalarenapos", (sender, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("You must be a player to use this command");
                return;
            }

            if (args.length != 2) {
                sender.sendMessage("Usage: /sg setfinalarenapos <id> <spec|gm|team1|team2>");
                return;
            }

            BlockVector3 pos = WorldEditService.getInstance().wrapLocation(player.getLocation());
            switch (args[1]) {
                case "spec" -> {
                    SGService.getInstance().setFinalArenaPosSpec(args[0], pos);
                    sender.sendMessage("Set final arena spectator spawn location to " + pos);
                }
                case "gm" -> {
                    SGService.getInstance().setFinalArenaPosGameMaster(args[0], pos);
                    sender.sendMessage("Set final arena game master spawn location to " + pos);
                }
                case "team1" -> {
                    SGService.getInstance().setFinalArenaPosTeam1(args[0], pos);
                    sender.sendMessage("Set final arena team 1 spawn location to " + pos);
                }
                case "team2" -> {
                    SGService.getInstance().setFinalArenaPosTeam2(args[0], pos);
                    sender.sendMessage("Set final arena team 2 spawn location to " + pos);
                }
                default -> sender.sendMessage("Invalid type " + args[0]);
            }
        },
        "clearmapspawns", (sender, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("You must be a player to use this command");
                return;
            }

            SGService.getInstance().clearMapSpawns(player.getWorld().getName());
            sender.sendMessage("Cleared map spawns");
        },
        "addmapspawn", (sender, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("You must be a player to use this command");
                return;
            }

            BlockVector3 pos = WorldEditService.getInstance().wrapLocation(player.getLocation());
            SGService.getInstance().addMapSpawn(player.getWorld().getName(), pos);
            sender.sendMessage("Added map spawn at " + pos);
        },
        "setmapmiddle", (sender, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("You must be a player to use this command");
                return;
            }

            BlockVector3 pos = WorldEditService.getInstance().wrapLocation(player.getLocation());
            SGService.getInstance().setMapMiddle(player.getWorld().getName(), pos);
            sender.sendMessage("Set map middle to " + pos);
        },
        "setmapworldborder", (sender, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("You must be a player to use this command");
                return;
            }

            if (args.length != 2) {
                sender.sendMessage("Usage: /sg setmapworldborder <min> <max> (note: min/max are diameters, not radius)");
                return;
            }

            SGService.getInstance().setMapWorldborder(player.getWorld().getName(), Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            sender.sendMessage("Set map worldborder to " + args[0] + "/" + args[1]);
        },
        "load", (sender, args) -> {
            SGService.getInstance().loadAllData();
            sender.sendMessage("Loaded arenas.json");
        },
        "save", (sender, args) -> {
            SGService.getInstance().saveAllData();
            sender.sendMessage("Saved arenas.json");
        }
    );

    private static final String HELP_MESSAGE = "Subcommands: " + String.join(", ", SUB_COMMANDS.keySet());

    protected SGDevCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(HELP_MESSAGE);
        } else if (SUB_COMMANDS.containsKey(args[0].toLowerCase())) {
            SUB_COMMANDS.get(args[0].toLowerCase()).accept(sender, Arrays.copyOfRange(args, 1, args.length));
        } else {
            sender.sendMessage(HELP_MESSAGE);
        }

        return true;
    }
}
