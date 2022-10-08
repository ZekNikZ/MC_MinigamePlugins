package io.zkz.mc.minigameplugins.gametools.inventory;

import io.zkz.mc.minigameplugins.gametools.inventory.opener.InventoryOpener;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.Optional;
import java.util.function.BiFunction;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

@Getter
@Accessors(fluent = true)
public class CustomUI {
    private final BiFunction<CustomUI, Player, UIContents> provider;
    private final String id;
    private final Component title;
    private final int rows;
    private final int cols;
    private final InventoryType type;
    private final boolean closeable;
    private final CustomUI parent;

    private CustomUI(BiFunction<CustomUI, Player, UIContents> provider, String id, Component title, int rows, int cols, InventoryType type, boolean closeable, CustomUI parent) {
        this.provider = provider;
        this.id = id;
        this.title = title;
        this.rows = rows;
        this.cols = cols;
        this.type = type;
        this.closeable = closeable;
        this.parent = parent;
    }

    public Inventory open(Player player) {
        return this.open(player, 0);
    }

    public Inventory open(Player player, int page) {
        Optional<CustomUI> oldInv = InventoryService.getInstance().getInventory(player);

        oldInv.ifPresent(inv -> {
//            inv.getListeners().stream()
//                .filter(listener -> listener.getType() == InventoryCloseEvent.class)
//                .forEach(listener -> ((InventoryListener<InventoryCloseEvent>) listener)
//                    .accept(new InventoryCloseEvent(player.getOpenInventory())));

            InventoryService.getInstance().setInventory(player, null);
        });

        UIContents contents = this.provider.apply(this, player);
        contents.paginations().forEach(pagination -> pagination.page(page));

        InventoryService.getInstance().setContents(player, contents);
        contents.initialize();

        InventoryOpener opener = InventoryService.getInstance().getOpener(this.type)
            .orElseThrow(() -> new IllegalStateException("No opener found for the inventory type " + this.type.name()));
        Inventory handle = opener.open(this, player);

        InventoryService.getInstance().setInventory(player, this);

        return handle;
    }

    public void close(Player player) {
//        listeners.stream()
//            .filter(listener -> listener.getType() == InventoryCloseEvent.class)
//            .forEach(listener -> ((InventoryListener<InventoryCloseEvent>) listener)
//                .accept(new InventoryCloseEvent(player.getOpenInventory())));

        InventoryService.getInstance().setInventory(player, null);
        player.closeInventory();

        InventoryService.getInstance().setContents(player, null);
    }

    public static Builder builder(BiFunction<CustomUI, Player, UIContents> provider) {
        return new Builder(provider);
    }

    public static final class Builder {
        private final BiFunction<CustomUI, Player, UIContents> provider;
        private String id = null;
        private Component title = mm("");
        private int rows = 6;
        private int cols = 9;
        private InventoryType type = InventoryType.CHEST;
        private boolean closeable = true;
        private CustomUI parent;

        public Builder(BiFunction<CustomUI, Player, UIContents> provider) {
            this.provider = provider;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        public Builder size(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
            return this;
        }

        public Builder type(InventoryType type) {
            this.type = type;
            return this;
        }

        public Builder closeable(boolean closeable) {
            this.closeable = closeable;
            return this;
        }

        public Builder parent(CustomUI parent) {
            this.parent = parent;
            return this;
        }

        public CustomUI build() {
            return new CustomUI(
                provider,
                id,
                title,
                rows,
                cols,
                type,
                closeable,
                parent
            );
        }
    }
}
