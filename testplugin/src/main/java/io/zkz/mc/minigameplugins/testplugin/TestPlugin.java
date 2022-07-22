package io.zkz.mc.minigameplugins.testplugin;

import io.zkz.mc.minigameplugins.gametools.GTPlugin;
import io.zkz.mc.minigameplugins.testplugin.service.TestService;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.logging.Logger;

@Plugin(name = "TestPlugin", version = "0.1")
@Description("A test plugin for the GameTools ecosystem")
@Author("ZekNikZ")
public class TestPlugin extends GTPlugin<TestPlugin> {
    private static Logger logger;
    public static Logger logger() {
        return logger;
    }

    public TestPlugin() {
        // Services
        register(TestService.getInstance());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        logger = this.getLogger();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}