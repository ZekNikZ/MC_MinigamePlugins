package io.zkz.mc.minigameplugins.gametools.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.TreeMap;

public class JSONDataManager<T extends PluginService<?>> extends FileBasedDataManager<T> {
    private final Serializer serializer;
    private final Deserializer deserializer;
    private final JSONParser parser = new JSONParser();

    @FunctionalInterface
    public interface Serializer {
        JSONObject serialize();
    }

    @FunctionalInterface
    public interface Deserializer {
        void deserialize(TypedJSONObject<Object> json);
    }

    public JSONDataManager(T service, Path filePath, @Nullable Serializer serializer, @NotNull Deserializer deserializer) {
        super(service, filePath);
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    @SuppressWarnings("java:S112")
    @Override
    public void loadData() throws IOException {
        // Ensure the file exists before attempting to read it
        if (!this.doesFileExist()) {
            if (this.serializer == null) {
                return;
            }

            this.saveData();
        }

        // Read and deserialize the data
        try (Reader reader = new InputStreamReader(Files.newInputStream(this.filePath))) {
            JSONObject json = (JSONObject) this.parser.parse(reader);
            this.deserializer.deserialize(new TypedJSONObject<>(json, Object.class));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void saveData() throws IOException {
        if (this.serializer == null) {
            return;
        }

        super.saveData();

        // Serialize and write the data
        JSONObject json = this.serializer.serialize();
        TreeMap<String, Object> treeMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        treeMap.putAll(json);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = Files.newBufferedWriter(this.filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            gson.toJson(treeMap, writer);
        }
    }
}
