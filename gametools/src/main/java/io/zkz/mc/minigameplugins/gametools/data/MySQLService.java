package io.zkz.mc.minigameplugins.gametools.data;

import com.mysql.cj.jdbc.MysqlDataSource;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import org.bukkit.ChatColor;
import org.json.simple.JSONObject;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MySQLService extends GameToolsService {
    private static final MySQLService INSTANCE = new MySQLService();
    private final List<String> setupList = new ArrayList<>();
    private boolean isAlreadySetup = false;

    public static MySQLService getInstance() {
        return INSTANCE;
    }

    public record DBConfig(String host, int port, String database, String username, String password) {
    }

    private DBConfig config = new DBConfig("localhost", 3306, "gametools", "root", "password");

    private MysqlDataSource dataSource;

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            new JSONDataManager<>(this, Path.of("db.json"), this::saveConfig, this::loadConfig)
        );
    }

    private void loadConfig(TypedJSONObject<Object> json) {
        this.config = new DBConfig(
            json.getString("host"),
            json.getInteger("port"),
            json.getString("database"),
            json.getString("username"),
            json.getString("password")
        );
    }

    private JSONObject saveConfig() {
        return new JSONObject(Map.of(
            "host", this.config.host(),
            "port", this.config.port(),
            "database", this.config.database(),
            "username", this.config.username(),
            "password", this.config.password()
        ));
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    public void onEnable() {
        // Load data source
        this.dataSource = new MysqlDataSource();
        this.dataSource.setServerName(this.config.host());
        this.dataSource.setPort(this.config.port());
        this.dataSource.setDatabaseName(this.config.database());
        this.dataSource.setUser(this.config.username());
        this.dataSource.setPassword(this.config.password());

        // Init database
        this.setupList.forEach(this::runInitCommands);

        this.isAlreadySetup = true;
    }

    public void addInitCommands(String setupString) {
        if (this.isAlreadySetup) {
            this.runInitCommands(setupString);
        } else {
            this.setupList.add(setupString);
        }
    }

    private void runInitCommands(String setupString) {
        // Mariadb can only handle a single query per statement. We need to split at ;.
        String[] queries = setupString.split(";");
        // execute each query to the database.
        for (String query : queries) {
            // If you use the legacy way you have to check for empty queries here.
            if (query.isBlank()) continue;
            try (Connection conn = this.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.execute();
            } catch (SQLException e) {
                this.getLogger().log(Level.SEVERE, "Could not initialize database", e);
            }
        }
        this.getLogger().info(ChatColor.AQUA + "Database setup complete.");
    }
}
