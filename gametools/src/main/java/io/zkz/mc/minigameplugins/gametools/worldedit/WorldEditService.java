package io.zkz.mc.minigameplugins.gametools.worldedit;

import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;

public class WorldEditService extends GameToolsService {
    private static final WorldEditService INSTANCE = new WorldEditService();
    private static boolean loaded = false;

    public static WorldEditService getInstance() throws IllegalStateException {
        if (!loaded) {
            throw new IllegalStateException("World Edit service is not loaded. Is WorldEdit installed?");
        }

        return INSTANCE;
    }

    public static void markAsLoaded() {
        loaded = true;
    }
}
