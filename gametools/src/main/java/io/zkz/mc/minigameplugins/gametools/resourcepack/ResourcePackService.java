package io.zkz.mc.minigameplugins.gametools.resourcepack;

import com.google.common.hash.Hashing;
import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.JSONDataManager;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.http.HTTPService;
import io.zkz.mc.minigameplugins.gametools.http.ResourcePackHandler;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

@Service
public class ResourcePackService extends PluginService<GameToolsPlugin> {
    // TODO: this should probably be the resource pack hash instead
    private static final String URL = "/resourcepack/v1";

    private static final ResourcePackService INSTANCE = new ResourcePackService();

    public static ResourcePackService getInstance() {
        return INSTANCE;
    }

    private Path resourcePackPath;
    private boolean enabled;
    private byte[] hash;

    public Path getResourcePackPath() {
        return this.resourcePackPath;
    }

    @Override
    protected void onEnable() {
        if (!this.enabled) {
            return;
        }

        this.computeHash();
        this.getLogger().info("Loaded resource pack at " + this.resourcePackPath.toAbsolutePath() + " with hash " + byteArrayToHex(this.hash));

        HTTPService.getInstance().addHandler(URL, new ResourcePackHandler());
    }

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            new JSONDataManager<>(this, Path.of("resourcepack.json"), this::saveData, this::loadData)
        );
    }

    private JSONObject saveData() {
        return new JSONObject(Map.of(
            "enabled", this.enabled,
            "path", this.resourcePackPath != null ? this.resourcePackPath.toAbsolutePath().toString() : "none"
        ));
    }

    private void loadData(TypedJSONObject<Object> json) {
        this.enabled = json.getBoolean("enabled");
        this.resourcePackPath = Optional.ofNullable(json.getString("path")).map(Path::of).orElse(null);
    }

    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    private void computeHash() {
        try {
            this.hash = com.google.common.io.Files.asByteSource(this.resourcePackPath.toFile()).hash(Hashing.sha1()).asBytes();
        } catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Could not compute resource pack hash", e);
        }
    }

    private static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
