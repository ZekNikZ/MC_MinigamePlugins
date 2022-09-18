package io.zkz.mc.minigameplugins.gametools;

import io.zkz.mc.minigameplugins.gametools.reflection.Service;
import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;

@Service(value = GameToolsPlugin.PLUGIN_NAME, priority = 9)
public class MinigameConstantsService extends GameToolsService {
    private static final MinigameConstantsService INSTANCE = new MinigameConstantsService();

    public static MinigameConstantsService getInstance() {
        return INSTANCE;
    }

    private String name;
    private String id;

    @Override
    protected void setup() {
        this.setMinigameName("Minigame");
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
}
