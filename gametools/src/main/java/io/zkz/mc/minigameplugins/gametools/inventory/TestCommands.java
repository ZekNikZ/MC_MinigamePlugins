package io.zkz.mc.minigameplugins.gametools.inventory;

import io.zkz.mc.minigameplugins.gametools.command.CommandRegistry;
import io.zkz.mc.minigameplugins.gametools.inventory.item.ClickableItem;
import io.zkz.mc.minigameplugins.gametools.reflection.RegisterCommands;
import io.zkz.mc.minigameplugins.gametools.util.BukkitUtils;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TestCommands {
    private static class TestProvider extends InventoryContentProvider {
        private int i = 0;

        public TestProvider(CustomInventory inv, Player player) {
            super(inv, player);
        }

        @Override
        protected void init() {
            for (int i = 1; i < 5; i++) {
                this.set(i, i, ISB.stack(Material.EMERALD));
            }
            this.fillBorders(ClickableItem.of(ISB.stack(Material.DIAMOND)));
            this.set(3, 6, ClickableItem.of(ISB.stack(Material.IRON_INGOT), (clickType) -> {
                this.player.sendMessage(clickType.name());
            }));
            this.set(4, 6, ClickableItem.of(ISB.stack(Material.COPPER_INGOT), (clickType) -> {
                this.inv.close(this.player);
            }));
        }

        @Override
        protected void update() {
            this.set(2, 6, ISB.stack(Material.GOLD_INGOT, this.i));
            this.i = (this.i + 1) % 65;
        }
    }

    private static final CustomInventory customInventory = CustomInventory
        .builder(TestProvider::new)
        .closeable(false)
        .build();

    @RegisterCommands
    private static void registerCommands(CommandRegistry registry) {
        var builder = registry.newBaseCommand("testinv");

        registry.registerCommand(builder.handler(cmd -> {
            BukkitUtils.runNextTick(() -> {
                if (cmd.getSender() instanceof Player player) {
                    customInventory.open(player);
//                    FakeInventory fakeInventory = new FakeInventory();
//                    player.openInventory(fakeInventory.getInventory());
                }
            });
        }));
    }
}
