package io.zkz.mc.uhc.overrides;

import io.zkz.mc.minigameplugins.gametools.util.ISB;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class RecipeOverrides {
    public static void override(JavaPlugin plugin) {
        // Remove enchanted golden apple recipe
        removeRecipe(plugin, ISB.stack(Material.GOLDEN_APPLE, (short) 1));

        // Enchanted head recipe
        plugin.getServer().addRecipe(
            new ShapedRecipe(
                ISB.material(Material.ENCHANTED_GOLDEN_APPLE)
                    .name(mm("<light_purple>Golden Head"))
                    .lore(mm("Cooked with the blood of your foes"))
                    .build()
            )
                .shape("###", "#*#", "###")
                .setIngredient('#', Material.GOLD_INGOT)
                .setIngredient('*', Material.PLAYER_HEAD)
        );
    }

    private static void removeRecipe(JavaPlugin plugin, ItemStack result) {
        Iterator<Recipe> it = plugin.getServer().recipeIterator();
        Recipe recipe;
        while (it.hasNext()) {
            recipe = it.next();
            if (recipe != null && recipe.getResult().getType() == result.getType() && recipe.getResult().getDurability() == result.getDurability()) {
                it.remove();
            }
        }
    }
}
