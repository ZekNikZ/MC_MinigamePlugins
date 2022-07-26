package io.zkz.mc.minigameplugins.gametools.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import org.bukkit.Location;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class SchematicService extends PluginService<GameToolsPlugin> {
    private static final SchematicService INSTANCE = new SchematicService();
    private static boolean loaded = false;

    public static SchematicService getInstance() throws IllegalStateException {
        if (!loaded) {
            throw new IllegalStateException("World Edit service is not loaded. Is WorldEdit installed?");
        }

        return INSTANCE;
    }

    public static void markAsLoaded() {
        loaded = true;
    }

    private Map<String, Clipboard> schematics = new HashMap<>();

    public boolean preloadSchematic(String key, InputStream schematicStream) {
        Objects.requireNonNull(schematicStream, "Cannot load schematic from a null stream");

        this.getLogger().info("Attempting to pre-load schematic with key " + key);

        try (ClipboardReader reader = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(schematicStream)) {
            Clipboard clipboard = reader.read();
            this.schematics.put(key, clipboard);
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not load schematic", e);
            return false;
        }

        this.getLogger().info("Done load schematic.");

        return true;
    }

    public boolean placeSchematic(String key, Location location) {
        return this.placeSchematic(key, location, false);
    }

    public boolean placeSchematic(String key, Location location, boolean ignoreAirBlocks) {
        Objects.requireNonNull(this.schematics.get(key), "Specified schematic has not been pre-loaded");

        this.getLogger().info("Attempting to place schematic " + key + " at " + location);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(location.getWorld()))) {
            Operation operation = new ClipboardHolder(this.schematics.get(key))
                .createPaste(editSession)
                .to(BlockVector3.at(location.getBlockX(), location.getY(), location.getZ()))
                .ignoreAirBlocks(ignoreAirBlocks)
                .build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            this.getLogger().log(Level.SEVERE, "Could not place schematic", e);
            return false;
        }

        this.getLogger().info("Done placing schematic.");

        return true;
    }

    public boolean placeSchematic(InputStream schematicStream, Location location) {
        return this.placeSchematic(schematicStream, location, false);
    }

    public boolean placeSchematic(InputStream schematicStream, Location location, boolean ignoreAirBlocks) {
        Objects.requireNonNull(schematicStream, "Cannot load schematic from a null stream");

        this.getLogger().info("Attempting to place schematic at " + location);

        try (ClipboardReader reader = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(schematicStream)) {
            Clipboard clipboard = reader.read();
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(location.getWorld()))) {
                Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(location.getBlockX(), location.getY(), location.getZ()))
                    .ignoreAirBlocks(ignoreAirBlocks)
                    .build();
                Operations.complete(operation);
            }
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not load schematic", e);
            return false;
        } catch (WorldEditException e) {
            this.getLogger().log(Level.SEVERE, "Could not place schematic", e);
            return false;
        }

        this.getLogger().info("Done placing schematic.");

        return true;
    }
}
