package io.zkz.mc.minigameplugins.gametools.data;

import com.mysql.cj.jdbc.MysqlDataSource;
import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.service.PluginServiceWithConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

@Service(priority = 10)
public class MySQLService extends PluginServiceWithConfig<GameToolsPlugin, MySQLService.DBConfig> {
    private static final MySQLService INSTANCE = new MySQLService();
    private final List<String> setupList = new ArrayList<>();
    private boolean isAlreadySetup = false;

    public static MySQLService getInstance() {
        return INSTANCE;
    }

    @Data
    @AllArgsConstructor
    @Accessors(fluent = true)
    public static final class DBConfig {
        private String host;
        private int port;
        private String database;
        private String username;
        private String password;
    }

    private MysqlDataSource dataSource;

    @Override
    protected Collection<AbstractDataManager<?>> getDataManagers() {
        return List.of(
            JSONDataManager.from(
                this,
                Path.of("db.json"),
                DBConfig.class,
                () -> new DBConfig("localhost", 3306, "gametools", "root", "password")
            )
        );
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @Override
    protected void onEnable() {
        // Load data source
        this.dataSource = new MysqlDataSource();
        this.dataSource.setServerName(this.getConfig().host());
        this.dataSource.setPort(this.getConfig().port());
        this.dataSource.setDatabaseName(this.getConfig().database());
        this.dataSource.setUser(this.getConfig().username());
        this.dataSource.setPassword(this.getConfig().password());

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
        this.getLogger().info("Database setup complete.");
    }
}
