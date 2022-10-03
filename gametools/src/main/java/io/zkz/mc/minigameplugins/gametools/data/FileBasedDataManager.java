package io.zkz.mc.minigameplugins.gametools.data;

import io.zkz.mc.minigameplugins.gametools.service.PluginService;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class FileBasedDataManager<T extends PluginService<?>> extends AbstractDataManager<T> {
    protected final Path filePath;

    protected FileBasedDataManager(T service, Path filePath) {
        super(service);
        if (!filePath.isAbsolute()) {
            this.filePath = service.getPlugin().getDataFolder().toPath().resolve(filePath);
        } else {
            this.filePath = filePath;
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void saveData() throws IOException {
        this.ensureParentFolderExists();
    }

    protected void ensureParentFolderExists() throws IOException {
        if (!Files.exists(this.filePath.getParent())) {
            Files.createDirectories(this.filePath.getParent());
        }
    }

    protected boolean doesFileExist() {
        return Files.exists(this.filePath);
    }
}
