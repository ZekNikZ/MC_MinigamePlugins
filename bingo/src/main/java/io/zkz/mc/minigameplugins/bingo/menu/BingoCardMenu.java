package io.zkz.mc.minigameplugins.bingo.menu;


import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.SlotIterator;
import io.zkz.mc.minigameplugins.bingo.BingoRound;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import io.zkz.mc.minigameplugins.gametools.util.ItemStackBuilder;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.Random;

public class BingoCardMenu implements InventoryProvider {
    public static Inventory open(Player player) {
        return open(player, null);
    }

    public static SmartInventory buildInventory() {
        return buildInventory(null);
    }

    public static Inventory open(Player player, SmartInventory parent) {
        return buildInventory(parent).open(player);
    }

    public static SmartInventory buildInventory(SmartInventory parent) {
        return SmartInventory.builder()
            .provider(new BingoCardMenu())
            .size(6, 9)
            .title("Choose an item")
            .parent(parent)
            .build();
    }

    private BingoCardMenu() {
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        this.drawCard(contents);
        this.drawControls(player, contents);
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        this.drawCard(contents);
    }

    private void drawCard(InventoryContents contents) {
        BingoRound round = ((BingoRound) MinigameService.getInstance().getCurrentRound());

        // Get the card
        List<ClickableItem> card = ((BingoRound) MinigameService.getInstance().getCurrentRound()).getCard().getItems().stream()
            .map(item -> {
                int points = round.getPointsForItem(item);
                ItemStackBuilder builder = ISB.material(item)
                    .amount(Math.max(points, 1));
                if (points == 0) {
                    builder.name(ChatColor.RED + "LOCKED OUT");
                }
                return ClickableItem.empty(
                    builder.build()
                );
            }).toList();

        // Draw the card
        final int row = 1;
        final int col = 1;
        for (int i = 0; i < 25; i++) {
            contents.set(row + (i / 5), col + (i % 5), card.get(i));
        }
    }

    private void drawControls(Player player, InventoryContents contents) {
        SlotIterator iter = contents.newIterator(SlotIterator.Type.VERTICAL, 1, 7);

        BingoRound round = ((BingoRound) MinigameService.getInstance().getCurrentRound());
        GameTeam team = TeamService.getInstance().getTeamOfPlayer(player);
        List<Player> teammates = TeamService.getInstance().getOnlineTeamMembers(team.getId()).stream()
            .filter(p -> !p.getUniqueId().equals(player.getUniqueId())).toList();

        iter.set(ClickableItem.of(
            ISB.material(Material.NETHER_STAR).name("Teleport to Spawn").build(),
            event -> {
                contents.inventory().close(player);
                player.teleport(round.getSpawnLocation());
            }
        ));
        iter.next();

        teammates.forEach(teammate -> {
            iter.next();
            iter.set(ClickableItem.of(
                ISB.material(Material.PLAYER_HEAD).name("Teleport to " + teammate.getDisplayName()).meta(itemMeta -> {
                    SkullMeta meta = ((SkullMeta) itemMeta);
                    meta.setOwningPlayer(teammate.getPlayer());
                }).build(),
               event -> {
                   contents.inventory().close(player);
                   player.teleport(teammates.get(new Random().nextInt(teammates.size())));
               }
            ));
        });
    }
}
