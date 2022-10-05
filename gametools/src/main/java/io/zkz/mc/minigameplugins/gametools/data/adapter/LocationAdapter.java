package io.zkz.mc.minigameplugins.gametools.data.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;

public class LocationAdapter extends TypeAdapter<Location> {
    @Override
    public void write(JsonWriter writer, Location value) throws IOException {
        writer.beginObject();

        writer.name("world");
        writer.value(value.getWorld().getName());

        writer.name("x");
        writer.value(value.getX());

        writer.name("y");
        writer.value(value.getY());

        writer.name("z");
        writer.value(value.getZ());

        writer.endObject();
    }

    @Override
    public Location read(JsonReader reader) throws IOException {
        String world = "world";
        double x = 0, y = 0, z = 0;

        reader.beginObject();

        String fieldName = null;

        while (reader.hasNext()) {
            JsonToken token = reader.peek();

            if (token.equals(JsonToken.NAME)) {
                fieldName = reader.nextName();
            }

            if ("world".equals(fieldName)) {
                token = reader.peek();
                world = reader.nextString();
            }

            if ("x".equals(fieldName)) {
                token = reader.peek();
                x = reader.nextDouble();
            }

            if ("y".equals(fieldName)) {
                token = reader.peek();
                y = reader.nextDouble();
            }

            if ("z".equals(fieldName)) {
                token = reader.peek();
                z = reader.nextDouble();
            }
        }

        reader.endObject();

        return new Location(Bukkit.getWorld(world), x, y, z);
    }
}
