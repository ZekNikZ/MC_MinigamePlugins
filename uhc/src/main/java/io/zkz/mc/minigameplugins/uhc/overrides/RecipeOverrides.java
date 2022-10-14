package io.zkz.mc.minigameplugins.uhc.overrides;

import io.zkz.mc.minigameplugins.gametools.util.ISB;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class RecipeOverrides {
    public static void override(JavaPlugin plugin) {
        // Remove enchanted golden apple recipe
        var key = removeRecipe(plugin, ISB.stack(Material.ENCHANTED_GOLDEN_APPLE));
        if (key == null) {
            key = Material.ENCHANTED_GOLDEN_APPLE.getKey();
        }

        // Enchanted head recipe
        plugin.getServer().addRecipe(
            new ShapedRecipe(
                key,
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

    private static NamespacedKey removeRecipe(JavaPlugin plugin, ItemStack result) {
        Iterator<Recipe> it = plugin.getServer().recipeIterator();
        Recipe recipe;
        while (it.hasNext()) {
            recipe = it.next();
            if (recipe != null && recipe.getResult().getType() == result.getType()) {
                it.remove();
                if (recipe.getResult() instanceof Keyed keyed) {
                    return keyed.getKey();
                }
                return recipe.getResult().getType().getKey();
            }
        }
        return null;
    }
}
