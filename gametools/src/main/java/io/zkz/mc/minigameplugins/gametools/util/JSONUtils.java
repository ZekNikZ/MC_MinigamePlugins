package io.zkz.mc.minigameplugins.gametools.util;

import com.sk89q.worldedit.math.BlockVector3;
import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class JSONUtils {
    private static final JSONParser parser = new JSONParser();

    public static List<Long> toJSON(BlockVector3 vec) {
        return List.of(
            (long) vec.getX(),
            (long) vec.getY(),
            (long) vec.getZ()
        );
    }

    public static Map<String, Object> toJSON(Location loc) {
        return Map.of(
            "world", loc.getWorld().getName(),
            "pos", List.of(
                (long) loc.getBlockX(),
                (long) loc.getBlockY(),
                (long) loc.getBlockZ()
            )
        );
    }

    public static Location readLocation(JSONObject json) {
        TypedJSONObject<Object> rawLoc = new TypedJSONObject<>(json, Object.class);
        List<Long> pos = rawLoc.getList("pos", Long.class);
        return new Location(
            Bukkit.getWorld(rawLoc.getString("world")),
            pos.get(0), pos.get(1), pos.get(2)
        );
    }

    public static BlockVector3 readBlockVector(List<Long> json) {
        return BlockVector3.at(json.get(0), json.get(1), json.get(2));
    }

    public static JSONObject readJSONObject(InputStream inputStream) {
        try (Reader reader = new InputStreamReader(inputStream)) {
            return (JSONObject) parser.parse(reader);
        } catch (IOException | ParseException e) {
            GameToolsPlugin.logger().log(Level.SEVERE, "Could not load JSON", e);
            throw new RuntimeException();
        }
    }

    public static JSONArray readJSONArray(InputStream inputStream) {
        try (Reader reader = new InputStreamReader(inputStream)) {
            return (JSONArray) parser.parse(reader);
        } catch (IOException | ParseException e) {
            GameToolsPlugin.logger().log(Level.SEVERE, "Could not load JSON", e);
            throw new RuntimeException();
        }
    }
}
