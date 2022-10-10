package io.zkz.mc.minigameplugins.gametools.util;

import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class ItemStackBuilder {
    private final ItemStack stack;
    private final List<Component> lore = new ArrayList<>();
    private Component name = null;
    private Boolean unbreakable = null;
    private Integer damage = null;
    private final List<Material> canPlaceOn = new ArrayList<>();
    private final List<Material> canBreak = new ArrayList<>();

    private ItemStackBuilder(ItemStack stack) {
        this.stack = stack.clone();

        var prevMeta = stack.getItemMeta();
        if (prevMeta == null) {
            return;
        }

        // Copy previous lore
        if (prevMeta.hasLore()) {
            this.lore.addAll(prevMeta.lore());
        }

        // TODO: copy CanPlaceOn and CanBreak
    }

    public static ItemStackBuilder builder() {
        return fromMaterial(Material.AIR);
    }

    public static ItemStackBuilder fromMaterial(Material material) {
        return new ItemStackBuilder(new ItemStack(material));
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

    public ItemStackBuilder damage(int damage) {
        this.damage = damage;
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

    public ItemStackBuilder potion(PotionData potionData) {
        return this.meta(itemMeta -> ((PotionMeta) itemMeta).setBasePotionData(potionData));
    }

    public ItemStackBuilder name(Component name) {
        this.name = mm("<!i><0>", name);
        return this;
    }

    public ItemStackBuilder lore(Component lore) {
        this.lore.add(mm("<!i><gray><0>", lore));
        return this;
    }

    public ItemStackBuilder lore(List<Component> lore) {
        lore.forEach(this::lore);
        return this;
    }

    public ItemStackBuilder lore(Component... lore) {
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

    @SuppressWarnings("deprecation")
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

    public ItemStackBuilder addItemFlags(ItemFlag... itemFlags) {
        this.stack.addItemFlags(itemFlags);
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
        ItemStack result = this.stack.clone();
        ItemMeta meta = result.getItemMeta();

        if (this.name != null) {
            meta.displayName(this.name);
        }

        if (!this.lore.isEmpty()) {
            meta.lore(this.lore);
        }

        if (this.unbreakable != null) {
            meta.setUnbreakable(this.unbreakable);
        }

        if (this.damage != null) {
            ((Damageable) meta).setDamage(this.damage);
        }

        result.setItemMeta(meta);

        if (!this.canPlaceOn.isEmpty() || !this.canBreak.isEmpty()) {
            net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(result);
            CompoundTag tag = nmsStack.getOrCreateTag();

            if (!this.canPlaceOn.isEmpty()) {
                ListTag canPlaceOnTag = new ListTag();
                this.canPlaceOn.forEach(mat -> canPlaceOnTag.add(StringTag.valueOf(mat.getKey().toString())));
                tag.put("CanPlaceOn", canPlaceOnTag);
            }

            if (!this.canBreak.isEmpty()) {
                ListTag canBreakTag = new ListTag();
                this.canBreak.forEach(mat -> canBreakTag.add(StringTag.valueOf(mat.getKey().toString())));
                tag.put("CanDestroy", canBreakTag);
            }

            nmsStack.setTag(tag);
            result = CraftItemStack.asBukkitCopy(nmsStack);
        }

        return result;
    }
}
