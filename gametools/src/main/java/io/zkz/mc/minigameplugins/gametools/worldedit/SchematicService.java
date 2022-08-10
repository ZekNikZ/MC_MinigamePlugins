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
import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import org.bukkit.Location;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.logging.Level;

public class SchematicService extends GameToolsService {
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

    public boolean loadSchematic(InputStream schematicStream, Location placementLocation) {
        Objects.requireNonNull(schematicStream, "Cannot load schematic from a null stream");

        try (ClipboardReader reader = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(schematicStream)) {
            Clipboard clipboard = reader.read();

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(new BukkitWorld(placementLocation.getWorld()))) {
                Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(placementLocation.getBlockX(), placementLocation.getY(), placementLocation.getZ()))
                    .ignoreAirBlocks(false)
                    .build();
                Operations.complete(operation);
            }
        } catch (IOException | WorldEditException e) {
            this.getLogger().log(Level.SEVERE, "Could not load schematic", e);
            return false;
        }

        return true;
    }
}
