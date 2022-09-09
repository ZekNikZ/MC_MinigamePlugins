package io.zkz.mc.minigameplugins.gametools;

import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import net.md_5.bungee.api.ChatColor;

public class MinigameConstantsService extends GameToolsService {
    private static final MinigameConstantsService INSTANCE = new MinigameConstantsService();

    public static MinigameConstantsService getInstance() {
        return INSTANCE;
    }

    private String name;
    private String id;

    private String chatPrefixFormat = ChatColor.GRAY + "[" + ChatColor.AQUA + "%s" + ChatColor.GRAY + "]" + ChatColor.RESET + " ";
    private String scoreboardTitleFormat = "" + ChatColor.GOLD + ChatColor.BOLD + "%s";

    @Override
    protected void setup() {
        this.setMinigameName("Minigame");
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    public void setMinigameName(String name) {
        this.name = name;
    }

    public String getMinigameName() {
        return this.name;
    }

    public void setMinigameID(String id) {
        this.id = id;
    }

    public String getMinigameID() {
        return this.id;
    }

    public String getChatPrefix() {
        return this.chatPrefixFormat.formatted(this.name);
    }

    public String getScoreboardTitle() {
        return this.scoreboardTitleFormat.formatted(this.name);
    }

    public void setChatPrefixFormat(String format) {
        this.chatPrefixFormat = format;
    }

    public void setScoreboardTitle(String format) {
        this.scoreboardTitleFormat = format;
    }
}
