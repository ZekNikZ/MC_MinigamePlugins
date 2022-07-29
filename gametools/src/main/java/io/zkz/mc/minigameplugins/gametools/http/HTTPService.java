package io.zkz.mc.minigameplugins.gametools.http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.zkz.mc.minigameplugins.gametools.data.AbstractDataManager;
import io.zkz.mc.minigameplugins.gametools.data.JSONDataManager;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class HTTPService extends GameToolsService {
    private static final HTTPService INSTANCE = new HTTPService();

    public static HTTPService getInstance() {
        return INSTANCE;
    }

    private boolean enabled = false;
    private int port = 25566;
    private String host = "http://localhost";
    private final Map<String, HttpHandler> handlers = new HashMap<>();
    private HttpServer server;

    public void addHandler(String context, HttpHandler handler) {
        this.handlers.put(context, handler);
        this.getLogger().info("Added HTTP handler for " + context);
        this.enabled = true;
    }

    @Override
    public void onEnable() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.getPlugin(), () -> {
            if (!this.enabled) {
                return;
            }

            try {
                this.server = HttpServer.create(new InetSocketAddress(this.port), 0);
            } catch (IOException e) {
                this.getLogger().log(Level.SEVERE, "Could not create HTTP server", e);
                return;
            }
            this.handlers.forEach((context, handler) -> this.server.createContext(context, handler));
            this.server.setExecutor(null);
            this.server.start();
            this.getLogger().info("Started HTTP server on port " + this.port);
        }, 2);
    }

    @Override
    public void onDisable() {
        if (this.server != null) {
            this.server.stop(0);
            this.getLogger().info("Shut down HTTP server");
        }
    }

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            new JSONDataManager<>(this, Path.of("http-server.json"), this::saveConfig, this::loadConfig)
        );
    }

    private void loadConfig(TypedJSONObject<Object> json) {
        this.port = json.getInteger("port");
        this.host = json.getString("host");
    }

    private JSONObject saveConfig() {
        return new JSONObject(Map.of(
            "port", this.port,
            "host", this.host
        ));
    }

    public String getIP() {
        return this.host + ":" + this.port;
    }
}
