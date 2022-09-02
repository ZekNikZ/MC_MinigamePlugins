package io.zkz.mc.minigameplugins.gametools.util;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ItemStackBuilder {
    private final ItemStack stack;
    private final List<String> lore = new ArrayList<>();
    private String name = null;
    private Boolean unbreakable = null;
    private final List<Material> canPlaceOn = new ArrayList<>();
    private final List<Material> canBreak = new ArrayList<>();

    private ItemStackBuilder(ItemStack stack) {
        this.stack = stack.clone();
    }

    private ItemStackBuilder(Material material, short damage) {
        this.stack = new ItemStack(material, 1, damage);
    }

    public static ItemStackBuilder builder() {
        return fromMaterial(Material.AIR);
    }

    public static ItemStackBuilder fromMaterial(Material material) {
        return fromMaterial(material, (short) 0);
    }

    public static ItemStackBuilder fromMaterial(Material material, short damage) {
        return new ItemStackBuilder(material, damage);
    }

    public static ItemStackBuilder fromStack(ItemStack stack) {
        return new ItemStackBuilder(stack);
    }

    public ItemStackBuilder material(Material material) {
        this.stack.setType(material);
        return this;
    }

    public ItemStackBuilder amount(int count) {
        this.stack.setAmount(count);
        return this;
    }

    public ItemStackBuilder damage(short damage) {
        this.stack.setDurability(damage);
        return this;
    }

    public ItemStackBuilder meta(ItemMeta meta) {
        this.stack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder meta(Consumer<ItemMeta> metaModifier) {
        ItemMeta meta = this.stack.getItemMeta();
        metaModifier.accept(meta);
        this.stack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder name(String name) {
        this.name = ChatColor.RESET + name;
        return this;
    }

    public ItemStackBuilder unformattedName(String name) {
        this.name = name;
        return this;
    }

    public ItemStackBuilder unformattedLore(String lore) {
        this.lore.add(lore);
        return this;
    }

    public ItemStackBuilder unformattedLore(List<String> lore) {
        this.lore.addAll(lore);
        return this;
    }

    public ItemStackBuilder unformattedLore(String... lore) {
        return this.unformattedLore(Arrays.asList(lore));
    }

    public ItemStackBuilder lore(String lore) {
        this.lore.add("" + ChatColor.RESET + ChatColor.GRAY + lore);
        return this;
    }

    public ItemStackBuilder lore(List<String> lore) {
        this.lore.addAll(lore.stream().map(line -> "" + ChatColor.RESET + ChatColor.GRAY + line).toList());
        return this;
    }

    public ItemStackBuilder lore(String... lore) {
        return this.lore(Arrays.asList(lore));
    }

    public ItemStackBuilder unbreakable() {
        this.unbreakable = true;
        return this;
    }

    public ItemStackBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ItemStackBuilder skullOwner(OfflinePlayer player) {
        SkullMeta meta = ((SkullMeta) this.stack.getItemMeta());
        meta.setOwningPlayer(player);
        return this;
    }

    public ItemStackBuilder skullOwner(String name) {
        SkullMeta meta = ((SkullMeta) this.stack.getItemMeta());
        meta.setOwner(name);
        return this;
    }

    public ItemStackBuilder addEnchantment(Enchantment enchantment, int level) {
        this.stack.addEnchantment(enchantment, level);
        return this;
    }

    public ItemStackBuilder addEnchantments(Map<Enchantment, Integer> enchantments) {
        this.stack.addEnchantments(enchantments);
        return this;
    }

    public ItemStackBuilder addUnsafeEnchantment(Enchantment enchantment, int level) {
        this.stack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemStackBuilder addUnsafeEnchantments(Map<Enchantment, Integer> enchantments) {
        this.stack.addUnsafeEnchantments(enchantments);
        return this;
    }

    public ItemStackBuilder canPlaceOn(Material... materials) {
        this.canPlaceOn.addAll(Arrays.asList(materials));
        return this;
    }

    public ItemStackBuilder canBreak(Material... materials) {
        this.canBreak.addAll(Arrays.asList(materials));
        return this;
    }

    public ItemStack build() {
        ItemStack stack = this.stack.clone();
        ItemMeta meta = stack.getItemMeta();

        if (this.name != null) {
            meta.setDisplayName(this.name);
        }

        if (!this.lore.isEmpty()) {
            meta.setLore(this.lore);
        }

        if (this.unbreakable != null) {
            meta.setUnbreakable(this.unbreakable);
        }

        stack.setItemMeta(meta);

        if (!this.canPlaceOn.isEmpty() || !this.canBreak.isEmpty()) {
            net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
            NBTTagCompound tag = nmsStack.v();

            if (!this.canPlaceOn.isEmpty()) {
                NBTTagList canPlaceOnTag = new NBTTagList();
                this.canPlaceOn.forEach(mat -> {
                    canPlaceOnTag.add(NBTTagString.a(mat.getKey().toString()));
                });
                tag.a("CanPlaceOn", canPlaceOnTag);
            }

            if (!this.canBreak.isEmpty()) {
                NBTTagList canBreakTag = new NBTTagList();
                this.canBreak.forEach(mat -> {
                    canBreakTag.add(NBTTagString.a(mat.getKey().toString()));
                });
                tag.a("CanDestroy", canBreakTag);
            }

            nmsStack.c(tag);
            stack = CraftItemStack.asBukkitCopy(nmsStack);
        }

        return stack;
    }
}
