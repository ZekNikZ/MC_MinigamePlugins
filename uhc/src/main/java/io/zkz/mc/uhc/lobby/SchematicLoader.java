package io.zkz.mc.uhc.lobby;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.io.IOException;

public class SchematicLoader {
    public static boolean loadLobby() {
        try (ClipboardReader reader = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(SchematicLoader.class.getResourceAsStream("/uhclobbynew.schem"))) {
            Clipboard clipboard = reader.read();

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(Bukkit.getWorlds().get(0)))) {
                Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(-10, 200, -10))
                    .ignoreAirBlocks(false)
                    .build();
                Operations.complete(operation);
            }
        } catch (IOException | WorldEditException e) {
            Bukkit.broadcastMessage(e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean clearLobby() {
        Region region = new CuboidRegion(
            new BukkitWorld(Bukkit.getWorlds().get(0)),
            BlockVector3.at(-10, 200, -10),
            BlockVector3.at(11, 221, 11)
        );
        Material block = Material.AIR;

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(region.getWorld())) {
            Pattern pat = BukkitAdapter.adapt(block.createBlockData());
            editSession.setBlocks(region, pat);
        } catch (WorldEditException e) {
            Bukkit.broadcastMessage(e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean loadSuddenDeath() {
        try (ClipboardReader reader = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(SchematicLoader.class.getResourceAsStream("/suddendeathnew.schem"))) {
            Clipboard clipboard = reader.read();

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(Bukkit.getWorlds().get(0)))) {
                Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(-20, 245, -20))
                    .ignoreAirBlocks(false)
                    .build();
                Operations.complete(operation);
            }
        } catch (IOException | WorldEditException e) {
            Bukkit.broadcastMessage(e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean clearSuddenDeath() {
        Region region = new CuboidRegion(
            new BukkitWorld(Bukkit.getWorlds().get(0)),
            BlockVector3.at(-20, 245, -20),
            BlockVector3.at(20, 255, 20)
        );
        Material block = Material.AIR;

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(region.getWorld())) {
            Pattern pat = BukkitAdapter.adapt(block.createBlockData());
            editSession.setBlocks(region, pat);
        } catch (WorldEditException e) {
            Bukkit.broadcastMessage(e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
