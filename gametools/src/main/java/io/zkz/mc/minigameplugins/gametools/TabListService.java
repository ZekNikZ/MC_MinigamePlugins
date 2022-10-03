package io.zkz.mc.minigameplugins.gametools;

import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class TabListService extends PluginService<GameToolsPlugin> {
    private static final TabListService INSTANCE = new TabListService();

    public static TabListService getInstance() {
        return INSTANCE;
    }

    private final Map<UUID, Integer> headerFooterCache = new HashMap<>();
    private final Map<UUID, List<Integer>> playerListCache = new HashMap<>();

    public record TabListEntry(Component component) {
    }

    @NotNull
    private Function<@NotNull Player, @NotNull Component> header = p -> Component.empty();

    @NotNull
    private Function<@NotNull Player, @NotNull Component> footer = p -> Component.empty();

    @NotNull
    private Function<@NotNull Player, @Nullable List<TabListEntry>> playerList = p -> null;

    public void setHeader(@NotNull Function<@NotNull Player, @NotNull Component> header) {
        this.header = header;
    }

    public void setHeader(@NotNull Supplier<@NotNull Component> header) {
        this.header = p -> header.get();
    }

    public void setHeader(@NotNull Component header) {
        this.header = p -> header;
    }

    public void setFooter(@NotNull Function<@NotNull Player, @NotNull Component> footer) {
        this.footer = footer;
    }

    public void setFooter(@NotNull Supplier<@NotNull Component> footer) {
        this.footer = p -> footer.get();
    }

    public void setFooter(@NotNull Component footer) {
        this.footer = p -> footer;
    }

    public void setPlayerList(@NotNull Function<@NotNull Player, @NotNull List<TabListEntry>> playerList) {
        this.playerList = playerList;
    }

    public void setPlayerList(@NotNull Supplier<@NotNull List<TabListEntry>> playerList) {
        this.playerList = p -> playerList.get();
    }

    public void setPlayerList(@NotNull List<TabListEntry> playerList) {
        this.playerList = p -> playerList;
    }

    public void update() {
        BukkitUtils.forEachPlayer(this::updatePlayer);
    }

    public void updatePlayer(Player player) {
        // Header and footer
        Component actualHeader = this.header.apply(player);
        Component actualFooter = this.header.apply(player);
        Integer headerFooterHash = Objects.hash(actualHeader, actualFooter);
        if (!Objects.equals(this.headerFooterCache.get(player.getUniqueId()), headerFooterHash)) {
            player.sendPlayerListHeaderAndFooter(actualHeader, actualFooter);
            this.headerFooterCache.put(player.getUniqueId(), headerFooterHash);
        }

        // TODO: Player list
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        this.updatePlayer(event.getPlayer());
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        this.headerFooterCache.remove(event.getPlayer().getUniqueId());
        this.playerListCache.remove(event.getPlayer().getUniqueId());
    }
}
