package io.zkz.mc.minigameplugins.gametools;

import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import org.bukkit.ChatColor;

public class MinigameConstantsService extends GameToolsService {
    private static final MinigameConstantsService INSTANCE = new MinigameConstantsService();

    public static MinigameConstantsService getInstance() {
        return INSTANCE;
    }

    private String prefix;

    @Override
    protected void setup() {
        this.setStandardPrefix("Minigame");
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setStandardPrefix(String prefix) {
        this.setPrefix(ChatColor.GRAY + "[" + ChatColor.AQUA + prefix + ChatColor.GRAY + "]" + ChatColor.RESET + " ");
    }


    public String getPrefix() {
        return this.prefix;
    }
}
