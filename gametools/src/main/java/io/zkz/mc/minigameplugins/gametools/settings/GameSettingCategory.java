package io.zkz.mc.minigameplugins.gametools.settings;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

public record GameSettingCategory(ItemStack display, Component name, Component description) {
}
