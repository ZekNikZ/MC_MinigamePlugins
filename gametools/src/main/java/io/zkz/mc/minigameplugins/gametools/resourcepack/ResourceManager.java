package io.zkz.mc.minigameplugins.gametools.resourcepack;

import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONArray;
import io.zkz.mc.minigameplugins.gametools.util.JSONUtils;
import io.zkz.mc.minigameplugins.gametools.util.Pair;
import io.zkz.mc.minigameplugins.gametools.util.ZipFileUtil;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResourceManager {
    private static final Path resourcesPath = Path.of("").resolve("build").resolve("resources");
    private static final Path resourcePackPath = Path.of("").resolve("build").resolve("pack.zip");
    private static int nextCharId = 0;
    private static final Map<Character, Pair<Integer, Integer>> charData = new HashMap<>();

    public static void main(String[] args) throws IOException {
        // Create folder
        if (!Files.exists(resourcesPath.getParent())) {
            Files.createDirectories(resourcesPath.getParent());
        }

        addResourcePackMetadata();
        compressResourcePack();
    }

    private static void addResourcePackMetadata() {
        // pack.mcmeta
        String mcmeta = new JSONObject(Map.of(
            "pack", Map.of(
                "pack_format", 9,
                "description", "Minigame resources"
            )
        )).toJSONString();
        addMiscResource("pack.mcmeta", new ByteArrayInputStream(mcmeta.getBytes(StandardCharsets.US_ASCII)));

        TypedJSONArray<JSONObject> providers = new TypedJSONArray<>(charData.entrySet().stream().map(entry -> new JSONObject(Map.of(
            "type", "bitmap",
            "file", "minecraft:custom/custom_character_" + ((int) entry.getKey()) + ".png",
            "ascent", entry.getValue().key(),
            "height", entry.getValue().value(),
            "chars", new TypedJSONArray<>(List.of(escapeJava("" + entry.getKey())))
        ))).toList());
        JSONObject baseFontFile = JSONUtils.readJSONObject(ResourceManager.class.getResourceAsStream("/resources/assets/minecraft/font/default.json"));
        providers.addAll(((List<JSONObject>) baseFontFile.get("providers")).stream().map(obj -> new JSONObject(Map.of(
            "type", obj.get("type"),
            "file", obj.get("file"),
            "ascent", obj.get("ascent"),
            "height", obj.get("height"),
            "chars", ((List<String>) obj.get("chars")).stream().map(ResourceManager::escapeJava).toList()
        ))).toList());
        String fontData = new JSONObject(Map.of(
            "providers", providers
        )).toJSONString().replace("\\\\", "\\").replace("\\/", "/");
        addMiscResource("assets/minecraft/font/default.json", new ByteArrayInputStream(fontData.getBytes(StandardCharsets.US_ASCII)));
        addMiscResource("assets/space/textures/font/space_nosplit.png", ResourceManager.class.getResourceAsStream("/resources/assets/space/textures/font/space_nosplit.png"));
        addMiscResource("assets/space/textures/font/space_split.png", ResourceManager.class.getResourceAsStream("/resources/assets/space/textures/font/space_split.png"));
    }

    public static void addMiscResource(String location, InputStream inputStream) {
        Path path = resourcesPath.resolve(location);
        if (!Files.exists(path.getParent())) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                System.err.println("Could not create asset '" + location + "'");
                e.printStackTrace(System.err);
            }
        }
        try (OutputStream os = new FileOutputStream(path.toFile())) {
            inputStream.transferTo(os);
        } catch (IOException e) {
            System.err.println("Could not create resource file '" + location + "'");
            e.printStackTrace(System.err);
        }
    }

    public void addBlockTexture(String blockId, InputStream inputStream) {
        String location = "assets/minecraft/textures/block/" + blockId;
        if (!location.contains(".png")) {
            location += ".png";
        }
        addMiscResource(location, inputStream);
    }

    public static char addCustomCharacterImage(char c, InputStream inputStream, int ascent, int height) {
        String location = "assets/minecraft/textures/custom/custom_character_" + nextCharId + ".png";
        ++nextCharId;

        if (charData.containsKey(c)) {
            System.err.println("Error: duplicate custom character '" + c + "'");
            return '\0';
        }

        addMiscResource(location, inputStream);
        charData.put(c, new Pair<>(ascent, height));

        return c;
    }

    private static void compressResourcePack() {
        try {
            ZipFileUtil.zipDirectory(resourcesPath.toFile(), resourcePackPath.toFile());
        } catch (IOException e) {
            System.err.println("Could not compress resource pack");
            e.printStackTrace(System.err);
        }
    }

    /**
     * The following are from Apache Commons. I couldn't figure out how to make them work with my Gradle configs.
     */
    private static String escapeJava(String str) {
        if (str == null) {
            return null;
        }
        try {
            StringWriter writer = new StringWriter(str.length() * 2);
            escapeJavaStyleString(writer, str, true, true);
            return writer.toString();
        } catch (IOException ignored) {
            return null;
        }
    }

    private static void escapeJavaStyleString(Writer out, String str, boolean escapeSingleQuote,
                                              boolean escapeForwardSlash) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("The Writer must not be null");
        }
        if (str == null) {
            return;
        }
        int sz;
        sz = str.length();
        for (int i = 0; i < sz; i++) {
            char ch = str.charAt(i);

            // handle unicode
            if (ch > 0xfff) {
                out.write("\\u" + hex(ch));
            } else if (ch > 0xff) {
                out.write("\\u0" + hex(ch));
            } else if (ch > 0x7f) {
                out.write("\\u00" + hex(ch));
            } else if (ch < 32) {
                switch (ch) {
                    case '\b' :
                        out.write('\\');
                        out.write('b');
                        break;
                    case '\n' :
                        out.write('\\');
                        out.write('n');
                        break;
                    case '\t' :
                        out.write('\\');
                        out.write('t');
                        break;
                    case '\f' :
                        out.write('\\');
                        out.write('f');
                        break;
                    case '\r' :
                        out.write('\\');
                        out.write('r');
                        break;
                    default :
                        if (ch > 0xf) {
                            out.write("\\u00" + hex(ch));
                        } else {
                            out.write("\\u000" + hex(ch));
                        }
                        break;
                }
            } else {
                switch (ch) {
                    case '\'' :
                        if (escapeSingleQuote) {
                            out.write('\\');
                        }
                        out.write('\'');
                        break;
                    case '"' :
                        out.write('\\');
                        out.write('"');
                        break;
                    case '\\' :
                        out.write('\\');
                        out.write('\\');
                        break;
                    case '/' :
                        if (escapeForwardSlash) {
                            out.write('\\');
                        }
                        out.write('/');
                        break;
                    default :
                        out.write(ch);
                        break;
                }
            }
        }
    }

    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
    }
}