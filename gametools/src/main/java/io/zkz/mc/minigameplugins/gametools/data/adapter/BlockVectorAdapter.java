package io.zkz.mc.minigameplugins.gametools.data.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.sk89q.worldedit.math.BlockVector3;

import java.io.IOException;

public class BlockVectorAdapter extends TypeAdapter<BlockVector3> {
    @Override
    public void write(JsonWriter writer, BlockVector3 value) throws IOException {
        writer.beginArray();

        writer.name("x");
        writer.value(value.getBlockX());

        writer.name("y");
        writer.value(value.getBlockY());

        writer.name("z");
        writer.value(value.getBlockZ());

        writer.endArray();
    }

    @Override
    public BlockVector3 read(JsonReader reader) throws IOException {
        int x = 0, y = 0, z = 0;

        reader.beginObject();

        String fieldName = null;

        while (reader.hasNext()) {
            JsonToken token = reader.peek();

            if (token.equals(JsonToken.NAME)) {
                fieldName = reader.nextName();
            }

            if ("x".equals(fieldName)) {
                token = reader.peek();
                x = reader.nextInt();
            }

            if ("y".equals(fieldName)) {
                token = reader.peek();
                y = reader.nextInt();
            }

            if ("z".equals(fieldName)) {
                token = reader.peek();
                z = reader.nextInt();
            }
        }

        reader.endObject();

        return BlockVector3.at(x, y, z);
    }
}
