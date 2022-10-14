package io.zkz.mc.minigameplugins.minigamemanager;

import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "MinigameManager", version = "2.0")
@Description("A plugin to manage minigame states")
@Author("ZekNikZ")
@ApiVersion(ApiVersion.Target.v1_19)
@Dependency("GameTools")
@SoftDependency("ProtocolLib")
@SoftDependency("SubServers-Client-Bukkit")
public class MinigameManagerPlugin extends GTPlugin<MinigameManagerPlugin> {
}