package io.zkz.mc.minigameplugins.gametools.data;

import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.logging.Level;

public class MySQLDataManager<T extends PluginService<?>> extends AbstractDataManager<T> {
    private final Consumer<Connection> loader;
    private final ConcurrentLinkedQueue<Consumer<Connection>> pendingActions = new ConcurrentLinkedQueue<>();

    private final int taskId;

    public MySQLDataManager(T service, @NotNull Consumer<Connection> loader) {
        super(service);
        this.loader = loader;

        this.taskId = Bukkit.getScheduler().scheduleAsyncRepeatingTask(this.service.getPlugin(), this::trySaveData, 20, 5);
    }

    @Override
    public void loadData() {
        try (Connection conn = MySQLService.getInstance().getConnection()) {
            this.loader.accept(conn);
        } catch (SQLException e) {
            this.service.getLogger().log(Level.SEVERE, "Could not load data from the database", e);
        }
    }

    @Override
    public void saveData() {
        // Since this data manager saves immediately, this method is unused
    }

    private void trySaveData() {
        if (this.pendingActions.isEmpty()) {
            return;
        }

        try (Connection conn = MySQLService.getInstance().getConnection()) {
            this.pendingActions.remove().accept(conn);
        } catch (SQLException e) {
            this.service.getLogger().log(Level.SEVERE, "Could not save data to the database", e);
        }
    }

    @Override
    public void cleanup() {
        Bukkit.getScheduler().cancelTask(this.taskId);
        this.trySaveData();
    }

    public void addAction(Consumer<Connection> action) {
        this.pendingActions.add(action);
    }
}
