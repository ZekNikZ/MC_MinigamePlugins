package io.zkz.mc.minigameplugins.battlebox;

import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.BlockUtils;
import io.zkz.mc.minigameplugins.gametools.util.ColorUtils;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.function.BiConsumer;

public class Kits {
    private static final Map<String, BiConsumer<Player, PlayerInventory>> KITS = Map.of(
        "base", (player, inv) -> {
            inv.addItem(
                ISB.material(Material.WOODEN_SWORD)
                    .unbreakable()
                    .build(),
                ISB.material(Material.BOW)
                    .unbreakable()
                    .build(),
                ISB.material(Material.SHEARS)
                    .unbreakable()
                    .canBreak(BlockUtils.allWools().toArray(Material[]::new))
                    .canBreak(leaves())
                    .build(),
                ISB.material(TeamService.getInstance().getTeamOfPlayer(player).getWoolColor())
                    .amount(64)
                    .canPlaceOn(BlockUtils.allWools().toArray(Material[]::new))
                    .canPlaceOn(validWoolPlacementBlocks())
                    .build(),
                ISB.material(leaves())
                    .amount(8)
                    .canPlaceOn(validPlacementBlocks())
                    .canPlaceOn(leaves())
                    .build(),
                ISB.stack(Material.ARROW, 6)
            );
            Color color = ColorUtils.toBukkitColor(TeamService.getInstance().getTeamOfPlayer(player).getColor());
            LeatherArmorMeta meta = ((LeatherArmorMeta) Bukkit.getItemFactory().getItemMeta(Material.LEATHER_BOOTS));
            meta.setColor(color);
            inv.setItem(
                EquipmentSlot.FEET,
                ISB.material(Material.LEATHER_BOOTS)
                    .meta(meta)
                    .unbreakable()
                    .build()
            );
        },
        "crossbow", (player, inv) -> {
            inv.remove(Material.BOW);
            inv.setItemInOffHand(
                ISB.material(Material.CROSSBOW)
                    .unbreakable()
                    .build());
            inv.addItem(
                ISB.stack(Material.ARROW, 1)
            );
        },
        "creepers", (player, inv) -> {
            inv.addItem(
                ISB.material(Material.CREEPER_SPAWN_EGG)
                    .amount(2)
                    .canPlaceOn(validPlacementBlocks())
                    .build()
            );
        },
        "speed", (player, inv) -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 0, true));
        },
        "cobwebs", (player, inv) -> {
            inv.addItem(
                ISB.material(Material.COBWEB)
                    .amount(4)
                    .canPlaceOn(validPlacementBlocks())
                    .canPlaceOn(Material.COBWEB)
                    .build()
            );
        }
    );

    public static void apply(String kit, Player player) {
        KITS.get(kit).accept(player, player.getInventory());
    }

    private static Material leaves() {
        return BattleBoxService.getInstance().getConfig().map().leafBlock();
    }

    private static Material[] validWoolPlacementBlocks() {
        return BattleBoxService.getInstance().getConfig().map().validWoolPlacementBlocks();
    }

    private static Material[] validPlacementBlocks() {
        return BattleBoxService.getInstance().getConfig().map().validPlacementBlocks();
    }
}
