package io.zkz.mc.minigameplugins.gametools.worldedit;

import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;

public class RegionService extends GameToolsService {
    private static final RegionService INSTANCE = new RegionService();
    private static boolean loaded = false;

    public static RegionService getInstance() throws IllegalStateException {
        if (!loaded) {
            throw new IllegalStateException("World Edit service is not loaded. Is WorldEdit installed?");
        }

        return INSTANCE;
    }

    public static void markAsLoaded() {
        loaded = true;
    }
}
