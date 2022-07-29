package io.zkz.mc.minigameplugins.gametools.resourcepack;

import com.google.common.hash.Hashing;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONArray;
import io.zkz.mc.minigameplugins.gametools.http.HTTPService;
import io.zkz.mc.minigameplugins.gametools.http.ResourcePackHandler;
import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.gametools.util.Pair;
import io.zkz.mc.minigameplugins.gametools.util.ZipFileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.IntStream;

public class ResourcePackService extends GameToolsService {
    // TODO: this should probably be the resource pack hash instead
    private static final String URL = "/resourcepack/" + System.currentTimeMillis();

    private static final ResourcePackService INSTANCE = new ResourcePackService();

    public static ResourcePackService getInstance() {
        return INSTANCE;
    }

    private Path resourcesPath;
    private Path resourcePackPath;
    private byte[] hash;
    private boolean enabled = false;
    private int nextCharId = 0;
    private final List<Pair<Integer, Integer>> charData = new ArrayList<>();

    public Path getResourcePackPath() {
        return this.resourcePackPath;
    }

    @Override
    protected void setup() {
        this.resourcesPath = this.getPlugin().getDataFolder().toPath().resolve("resources");
        this.resourcePackPath = this.getPlugin().getDataFolder().toPath().resolve("pack.zip");
    }

    @Override
    public void onEnable() {
        // Clean up existing resources
        try {
            if (!Files.exists(this.resourcesPath.getParent())) {
                Files.createDirectories(this.resourcesPath.getParent());
            }

            if (Files.exists(this.resourcesPath)) {
                FileUtils.deleteDirectory(this.resourcesPath.toFile());
            }

            if (Files.exists(this.resourcePackPath)) {
                Files.delete(this.resourcePackPath);
            }
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not cleanup resources", e);
        }

        BukkitUtils.runNextTick(() -> {
            if (!this.enabled) {
                return;
            }

            this.addResourcePackMetadata();
            this.compressResourcePack();
            this.computeHash();
            HTTPService.getInstance().addHandler(URL, new ResourcePackHandler());
        });
    }

    private void addResourcePackMetadata() {
        // pack.mcmeta
        String mcmeta = new JSONObject(Map.of(
            "pack", Map.of(
                "pack_format", 9,
                "description", "Minigame resources"
            )
        )).toJSONString();
        this.addMiscResource("pack.mcmeta", new ByteArrayInputStream(mcmeta.getBytes(StandardCharsets.US_ASCII)));

        // Custom font
        if (this.nextCharId == 0) {
            return;
        }
        String fontData = new JSONObject(Map.of(
            "providers", new TypedJSONArray<>(IntStream.range(0, this.charData.size()).mapToObj(i -> new JSONObject(Map.of(
                "type", "bitmap",
                "file", "minecraft:custom/custom_character_" + i + ".png",
                "ascent", this.charData.get(i).key(),
                "height", this.charData.get(i).value(),
                "chars",  new TypedJSONArray<>(List.of(StringEscapeUtils.escapeJava("" + (char) ('\uE000' + i))))
            ))).toList())
        )).toJSONString().replace("\\\\", "\\").replace("\\/", "/");
        this.addMiscResource("assets/minecraft/font/default.json", new ByteArrayInputStream(fontData.getBytes(StandardCharsets.US_ASCII)));
    }

    private void compressResourcePack() {
        try {
            ZipFileUtil.zipDirectory(this.resourcesPath.toFile(), this.resourcePackPath.toFile());
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not compress resource pack", e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void computeHash() {
        try {
            this.hash = com.google.common.io.Files.asByteSource(this.resourcePackPath.toFile()).hash(Hashing.sha1()).asBytes();
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not compute resource pack hash", e);
        }
    }

    public void addMiscResource(String location, InputStream inputStream) {
        Path path = this.resourcesPath.resolve(location);
        if (!Files.exists(path.getParent())) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                this.getLogger().log(Level.SEVERE, "Could not create asset '" + location + "'", e);
            }
        }
        try (OutputStream os = new FileOutputStream(path.toFile())) {
            inputStream.transferTo(os);
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not create resource file '" + location + "'", e);
        }
        this.enabled = true;
    }

    // TODO: implement other resource pack items:
    // sound
    // font/characters!!!
    // models - block/item
    // textures - block/item/...
    public void addBlockTexture(String blockId, InputStream inputStream) {
        String location = "assets/minecraft/textures/block/" + blockId;
        if (!location.contains(".png")) {
            location += ".png";
        }
        this.addMiscResource(location, inputStream);
    }

    public char addCustomCharacterImage(InputStream inputStream, int ascent, int height) {
        String location = "assets/minecraft/textures/custom/custom_character_" + this.nextCharId + ".png";
        char c = (char) ('\uE000' + this.nextCharId);
        ++this.nextCharId;

        this.addMiscResource(location, inputStream);
        this.charData.add(new Pair<>(ascent, height));

        return c;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        if (!this.enabled) {
            return;
        }

        String path = HTTPService.getInstance().getIP() + URL;
        String hash = byteArrayToHex(this.hash);

        event.getPlayer().setResourcePack(HTTPService.getInstance().getIP() + URL, this.hash, true);
        this.getLogger().info("Attempting to send player '" + event.getPlayer().getName() + "' resource pack '" + path + "' with hash '" + hash + "'");
    }

    private static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
