package io.zkz.mc.minigameplugins.uhc.settings.enums;

import io.zkz.mc.minigameplugins.gametools.settings.impl.EnumSetting;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public enum SpectatorMode implements EnumSetting.GameSettingEnum {
    NORMAL(mm("Default behavior"), mm("Spectator mode acts like in Vanilla"), ISB.stack(Material.LEATHER_HELMET)),
    SPECTATORS_SEE_INVENTORIES(mm("Spectators can see inventories"), mm("Spectators can view player's inventories while spectating them"), ISB.stack(Material.DIAMOND_HELMET)),
    TEAMS_SEE_INVENTORIES(mm("Team members can see inventories"), mm("Spectators can view alive teammates' inventories while spectating them"), ISB.stack(Material.GOLDEN_HELMET));

    private final Component label;
    private final Component description;
    private final ItemStack display;

    SpectatorMode(Component label, Component description, ItemStack display) {
        this.label = label;
        this.description = description;
        this.display = display;
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
