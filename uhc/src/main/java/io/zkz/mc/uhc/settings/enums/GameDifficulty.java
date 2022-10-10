package io.zkz.mc.uhc.settings.enums;

import io.zkz.mc.minigameplugins.gametools.settings.impl.EnumSetting;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import net.kyori.adventure.text.Component;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public enum GameDifficulty implements EnumSetting.GameSettingEnum {
    PEACEFUL(Difficulty.PEACEFUL, mm("Peaceful"), mm(""), ISB.stack(Material.WOODEN_SWORD)),
    EASY(Difficulty.EASY, mm("Easy"), mm(""), ISB.stack(Material.STONE_SWORD)),
    NORMAL(Difficulty.NORMAL, mm("Normal"), mm(""), ISB.stack(Material.IRON_SWORD)),
    HARD(Difficulty.HARD, mm("Hard"), mm(""), ISB.stack(Material.DIAMOND_SWORD));

    private final Difficulty difficulty;
    private final Component label;
    private final Component description;
    private final ItemStack display;

    GameDifficulty(Difficulty difficulty, Component label, Component description, ItemStack display) {
        this.difficulty = difficulty;
        this.label = label;
        this.description = description;
        this.display = display;
    }

    public Difficulty difficulty() {
        return this.difficulty;
    }

    @Override
    public Component label() {
        return this.label;
    }

    @Override
    public Component description() {
        return this.description;
    }

    @Override
    public ItemStack display() {
        return this.display;
    }
}
