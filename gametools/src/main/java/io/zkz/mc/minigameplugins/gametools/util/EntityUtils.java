package io.zkz.mc.minigameplugins.gametools.util;

import org.bukkit.entity.Entity;

public class EntityUtils {
    public static boolean isHostile(Entity entity) {
        return switch (entity.getType()) {
            case ELDER_GUARDIAN, WITHER_SKELETON, STRAY, HUSK, ZOMBIE_VILLAGER, SKELETON_HORSE, ZOMBIE_HORSE, EVOKER, VINDICATOR, ILLUSIONER, CREEPER, SKELETON, SPIDER, GIANT, ZOMBIE, SLIME, GHAST, ZOMBIFIED_PIGLIN, ENDERMAN, CAVE_SPIDER, SILVERFISH, BLAZE, MAGMA_CUBE, WITCH, ENDERMITE, GUARDIAN, SHULKER, PHANTOM, PUFFERFISH, DROWNED, PILLAGER, RAVAGER, HOGLIN, PIGLIN, WARDEN, PIGLIN_BRUTE, ZOGLIN ->
                true;
            default -> false;
        };
    }
}
