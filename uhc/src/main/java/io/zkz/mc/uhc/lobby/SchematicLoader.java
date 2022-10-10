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
import jdk.nashorn.internal.ir.Block;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.io.IOException;

public class SchematicLoader {
    public static boolean loadLobby() {
//        BlockVector3 v = new BlockVector3(-10, 200, -10);
//        World world = Bukkit.getWorlds().get(0);
//        BukkitWorld bukkitWorld = new BukkitWorld(world);
//        EditSession es = WorldEdit.getInstance().newEditSession(bukkitWorld);
//
//        try {
//
//            CuboidClipboard cb = ((MCEditSchematicFormat) SchematicFormat.MCEDIT).load(FileUtils.class.getResourceAsStream("/uhclobby.schematic"));
//            cb.paste(es, v, false);
//        } catch (IOException | DataException | MaxChangedBlocksException e) {
//            e.printStackTrace();
//            return false;
//        }

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
//        Vector v1 = new Vector(-10, 200, -10);
//        Vector v2 = new Vector(11, 221, 11);
//        World world = Bukkit.getWorlds().get(0);
//        BukkitWorld bukkitWorld = new BukkitWorld(world);
//        EditSession es = new EditSession(bukkitWorld, 20000000);
//
//        try {
//            es.setBlocks(new CuboidRegion(v1, v2), new BaseBlock(Material.AIR.getId()));
//        } catch (MaxChangedBlocksException e) {
//            e.printStackTrace();
//            return false;
//        }

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
//        Vector v = new Vector(-19, 254, -19);
//        World world = Bukkit.getWorlds().get(0);
//        BukkitWorld bukkitWorld = new BukkitWorld(world);
//        EditSession es = new EditSession(bukkitWorld, 20000000);
//
//        try {
//            CuboidClipboard cb = ((MCEditSchematicFormat) SchematicFormat.MCEDIT).load(FileUtils.class.getResourceAsStream("/suddendeath.schematic"));
//            cb.paste(es, v, false);
//        } catch (IOException | DataException | MaxChangedBlocksException e) {
//            e.printStackTrace();
//            return false;
//        }

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
//        Vector v1 = new Vector(-20, 245, -20);
//        Vector v2 = new Vector(20, 255, 20);
//        World world = Bukkit.getWorlds().get(0);
//        BukkitWorld bukkitWorld = new BukkitWorld(world);
//        EditSession es = new EditSession(bukkitWorld, 20000000);
//
//        try {
//            es.setBlocks(new CuboidRegion(v1, v2), new BaseBlock(Material.AIR.getId()));
//        } catch (MaxChangedBlocksException e) {
//            e.printStackTrace();
//            return false;
//        }

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
