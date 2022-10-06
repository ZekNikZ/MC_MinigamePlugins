package io.zkz.mc.minigameplugins.gametools.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldedit.math.BlockVector3;
import io.zkz.mc.minigameplugins.gametools.data.adapter.BlockVectorAdapter;
import io.zkz.mc.minigameplugins.gametools.data.adapter.LocationAdapter;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.service.PluginServiceWithConfig;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JSONDataManager<T extends PluginService<?>, C> extends FileBasedDataManager<T> {
    private static final Gson gson;

    static {
        var builder = new GsonBuilder()
                .setPrettyPrinting();
        
        builder.registerTypeAdapter(BlockVector3.class, new BlockVectorAdapter())
                .registerTypeAdapter(Location.class, new LocationAdapter());

        gson = builder.create();
    }

    private final Class<C> configType;
    private final Consumer<C> onLoad;
    private final @Nullable Supplier<C> currentValueSupplier;
    private final @Nullable Supplier<C> defaultValueSupplier;

    public JSONDataManager(T service, Path filePath, Class<C> configType, Consumer<C> onLoad) {
        this(service, filePath, configType, onLoad, null, null);
    }

    public JSONDataManager(T service, Path filePath, Class<C> configType, Consumer<C> onLoad, @Nullable Supplier<C> currentValueSupplier) {
        this(service, filePath, configType, onLoad, currentValueSupplier, null);
    }

    public JSONDataManager(T service, Path filePath, Class<C> configType, ConfigHolder<C> holder) {
        this(service, filePath, configType, holder, null);
    }

    public JSONDataManager(T service, Path filePath, Class<C> configType, ConfigHolder<C> holder, @Nullable Supplier<C> defaultValueSupplier) {
        this(service, filePath, configType, holder::setConfig, holder::getConfig, defaultValueSupplier);
    }

    public JSONDataManager(T service, Path filePath, Class<C> configType, Consumer<C> onLoad, @Nullable Supplier<C> currentValueSupplier, @Nullable Supplier<C> defaultValueSupplier) {
        super(service, filePath);
        this.configType = configType;
        this.onLoad = onLoad;
        this.currentValueSupplier = currentValueSupplier;
        this.defaultValueSupplier = defaultValueSupplier;
    }

    public static <V extends PluginServiceWithConfig<?, C>, C> JSONDataManager<V, C> from(V service, Path filePath, Class<C> configType) {
        return from(service, filePath, configType, null);
    }

    public static <V extends PluginServiceWithConfig<?, C>, C> JSONDataManager<V, C> from(V service, Path filePath, Class<C> configType, @Nullable Supplier<C> defaultValueSupplier) {
        return new JSONDataManager<>(service, filePath, configType, service, defaultValueSupplier);
    }

    @Override
    public void loadData() throws IOException {
        // Ensure the file exists before attempting to read it
        if (!this.doesFileExist()) {
            if (this.defaultValueSupplier == null) {
                return;
            }

            this.saveDefaults();
        }

        // Read and deserialize the data
        try (Reader reader = new InputStreamReader(Files.newInputStream(this.filePath))) {
            C result = gson.fromJson(reader, configType);
            this.onLoad.accept(result);
        }
    }

    @Override
    public void saveData() throws IOException {
        if (this.currentValueSupplier == null) {
            return;
        }

        super.saveData();

        // Serialize and write the data
        try (Writer writer = Files.newBufferedWriter(this.filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(this.currentValueSupplier.get(), writer);
        }
    }

    public void saveDefaults() throws IOException {
        if (this.defaultValueSupplier == null) {
            return;
        }

        super.saveData();

        // Serialize and write the data
        try (Writer writer = Files.newBufferedWriter(this.filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(this.defaultValueSupplier.get(), writer);
        }
    }
}
