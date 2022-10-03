package io.zkz.mc.minigameplugins.gametools.util;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;

public class WorldSyncUtils {
    private WorldSyncUtils() {
    }

    public static <T> void setGameRule(GameRule<T> rule, T value) {
        Bukkit.getWorlds().forEach(world -> world.setGameRule(rule, value));
    }

    public static void setWorldBorderCenter(double x, double y) {
        Bukkit.getWorlds().forEach(world -> world.getWorldBorder().setCenter(world.getEnvironment() == World.Environment.NETHER ? x / 8 : x, world.getEnvironment() == World.Environment.NETHER ? y / 8 : y));
    }

    public static void setWorldBorderSize(double newSize) {
        Bukkit.getWorlds().forEach(world -> world.getWorldBorder().setSize(world.getEnvironment() == World.Environment.NETHER ? newSize / 8 : newSize));
    }

    public static void setWorldBorderSize(double newSize, long seconds) {
        Bukkit.getWorlds().forEach(world -> world.getWorldBorder().setSize(world.getEnvironment() == World.Environment.NETHER ? newSize / 8 : newSize, seconds));
    }

    public static void setWorldBorderWarningTime(int seconds) {
        Bukkit.getWorlds().forEach(world -> world.getWorldBorder().setWarningTime(seconds));
    }

    public static void setWorldBorderWarningDistance(int distance) {
        Bukkit.getWorlds().forEach(world -> world.getWorldBorder().setWarningDistance(distance));
    }

    public static void setWorldBorderDamageAmount(double damage) {
        Bukkit.getWorlds().forEach(world -> world.getWorldBorder().setDamageAmount(damage));
    }

    public static void setWorldBorderDamageBuffer(double blocks) {
        Bukkit.getWorlds().forEach(world -> world.getWorldBorder().setDamageBuffer(blocks));
    }

    public static void resetWorldBorder() {
        Bukkit.getWorlds().forEach(world -> world.getWorldBorder().reset());
    }

    public static double getWorldBorderSize() {
        return Bukkit.getWorlds().get(0).getWorldBorder().getSize();
    }

    public static void stopWorldBorder() {
        setWorldBorderSize(getWorldBorderSize());
    }

    public static void setDifficulty(Difficulty difficulty) {
        Bukkit.getWorlds().forEach(world -> world.setDifficulty(difficulty));
    }

    public static void setTime(int time) {
        Bukkit.getWorlds().forEach(world -> world.setTime(time));
    }

    public static void setWeatherClear() {
        Bukkit.getWorlds().forEach(world -> {
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(0);
        });
    }

    public static void setWeatherRain() {
        Bukkit.getWorlds().forEach(world -> {
            world.setStorm(true);
            world.setThundering(false);
        });
    }

    public static void setWeatherStorm() {
        Bukkit.getWorlds().forEach(world -> {
            world.setStorm(true);
            world.setThundering(true);
        });
    }
}
