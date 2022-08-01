package io.zkz.mc.minigameplugins.gametools;

import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;
import org.bukkit.ChatColor;

public class ChatConstantsService extends GameToolsService {
    private static final ChatConstantsService INSTANCE = new ChatConstantsService();
    public static ChatConstantsService getInstance() {
        return INSTANCE;
    }


    private String name;

    private String chatPrefixFormat = ChatColor.GRAY + "[" + ChatColor.AQUA + "%s" + ChatColor.GRAY + "]" + ChatColor.RESET + " ";
    private String scoreboardTitleFormat = ChatColor.AQUA + "%s";

    @Override
    protected void setup() {
        this.setMinigameName("Minigame");
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    public void setMinigameName(String name) {
        this.name = name;
    }

    public String getMinigameName() {
        return this.name;
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

    public void setScoreboardTitleFormat(String format) {
        this.scoreboardTitleFormat = format;
    }
}
