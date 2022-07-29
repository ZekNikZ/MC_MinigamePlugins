package io.zkz.mc.minigameplugins.gametools.worldedit;

import io.zkz.mc.minigameplugins.gametools.service.GameToolsService;

public class SchematicService extends GameToolsService {
    private static final SchematicService INSTANCE = new SchematicService();
    private static boolean loaded = false;

    public static SchematicService getInstance() throws IllegalStateException {
        if (!loaded) {
            throw new IllegalStateException("World Edit service is not loaded. Is WorldEdit installed?");
        }

        return INSTANCE;
    }

    public static void markAsLoaded() {
        loaded = true;
    }
}
