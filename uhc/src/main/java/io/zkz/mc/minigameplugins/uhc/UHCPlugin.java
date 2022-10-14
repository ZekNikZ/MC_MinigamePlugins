package io.zkz.mc.minigameplugins.uhc;

import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import io.zkz.mc.minigameplugins.uhc.overrides.AllOverrides;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

@Plugin(name = "UHCPlugin", version = "2.0")
@Author("ZekNikZ")
@Dependency("GameTools")
@Dependency("MinigameManager")
@Dependency("WorldEdit")
@ApiVersion(ApiVersion.Target.v1_19)
public class UHCPlugin extends GTPlugin<UHCPlugin> {
    @Override
    public void onEnable() {
        super.onEnable();
        AllOverrides.override(this);
    }
}
