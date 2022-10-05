package io.zkz.mc.minigameplugins.gametools.resourcepack;

import com.google.gson.GsonBuilder;
import io.zkz.mc.minigameplugins.gametools.util.Pair;
import io.zkz.mc.minigameplugins.gametools.util.ZipFileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@SuppressWarnings({"java:S106", "java:S112"})
public class ResourceManager {
    private static final Path resourcesPath;
    private static final Path resourcePackPath;
    private static final Map<String, Pair<Integer, Integer>> charData = new HashMap<>();

    static {
        Path pwd = Path.of("");
        if (Files.exists(pwd.toAbsolutePath().getParent().resolve("build"))) {
            resourcesPath = pwd.toAbsolutePath().getParent().resolve("build").resolve("resources");
            resourcePackPath = Path.of("").toAbsolutePath().getParent().resolve("build").resolve("pack.zip");
        } else {
            resourcesPath = pwd.resolve("build").resolve("resources");
            resourcePackPath = Path.of("").resolve("build").resolve("pack.zip");
        }
    }

    public static void main(String[] args) throws IOException {
        // Create folder
        if (!Files.exists(resourcesPath.getParent())) {
            Files.createDirectories(resourcesPath.getParent());
        }

        discoverCustomCharacters();
        addResourcePackMetadata();
        compressResourcePack();
    }

    private static void discoverCustomCharacters() {
        Path customCharacterPath = resourcesPath.resolve("assets/minecraft/textures/custom");
        try (Stream<Path> s = Files.list(customCharacterPath)) {
            s.forEach(file -> {
                String fileName = file.getFileName().toString();
                if (fileName.contains("custom_character")) {
                    String[] parts = fileName.split("[_.]");
                    String charStr = parts[2];
                    int ascent = Integer.parseInt(parts[3]);
                    int height = Integer.parseInt(parts[4]);
                    charData.put(charStr, new Pair<>(ascent, height));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addResourcePackMetadata() throws IOException {
        var gson = new GsonBuilder().setPrettyPrinting().create();

        // pack.mcmeta
        String mcmeta = gson.toJson(Map.of(
            "pack", Map.of(
                "pack_format", 9,
                "description", "Minigame resources"
            )
        ));
        addMiscResource("pack.mcmeta", new ByteArrayInputStream(mcmeta.getBytes(StandardCharsets.US_ASCII)));

        // Construct font data
        FontData fontData = new FontData(new ArrayList<>());
        fontData.providers().addAll(charData.entrySet().stream().map(entry -> new FontProvider(
            "bitmap",
            "minecraft:custom/custom_character_" + entry.getKey() + "_" + entry.getValue().key() + "_" + entry.getValue().value() + ".png",
            entry.getValue().key(),
            entry.getValue().value(),
            List.of("\\u" + entry.getKey().toUpperCase())
        )).toList());
        try (Reader reader = new InputStreamReader(ResourceManager.class.getResourceAsStream("/resources/assets/minecraft/font/default.json"))) {
            var baseFont = gson.fromJson(reader, FontData.class);
            fontData.providers().addAll(baseFont.providers());
        }
        String fontDataStr = gson.toJson(fontData);

        // Write font data
        addMiscResource("assets/minecraft/font/default.json", new ByteArrayInputStream(fontDataStr.getBytes(StandardCharsets.US_ASCII)));
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
        try (OutputStream os = new FileOutputStream(path.toFile()); inputStream) {
            inputStream.transferTo(os);
        } catch (IOException e) {
            System.err.println("Could not create resource file '" + location + "'");
            e.printStackTrace(System.err);
        }
    }

    public static void setBlockTexture(String blockId, InputStream inputStream) {
        String location = "assets/minecraft/textures/block/" + blockId;
        if (!location.contains(".png")) {
            location += ".png";
        }
        addMiscResource(location, inputStream);
    }

    public static void addItemTexture(String itemId, InputStream inputStream) {
        String location = "assets/minecraft/textures/item/" + itemId;
        if (!location.contains(".png")) {
            location += ".png";
        }
        addMiscResource(location, inputStream);
    }

    public static char addCustomCharacterImage(char c, InputStream inputStream, int ascent, int height) {
        String escaped = escapeJava("" + c);
        if (escaped == null) {
            return '\0';
        }

        String charId = escaped.substring(2).toLowerCase();

        String location = "assets/minecraft/textures/custom/custom_character_" + charId + "_" + ascent + "_" + height + ".png";

        if (charData.containsKey(charId)) {
            System.err.println("Error: duplicate custom character '" + escapeJava("" + c) + "'");
            return '\0';
        }

        addMiscResource(location, inputStream);
        charData.put(charId, new Pair<>(ascent, height));

        return c;
    }

    private static void compressResourcePack() {
        try {
            ZipFileUtils.zipDirectory(resourcesPath.toFile(), resourcePackPath.toFile());
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


    /**
     * The following are from Apache Commons. I couldn't figure out how to make them work with my Gradle configs.
     */
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
                    case '\b':
                        out.write('\\');
                        out.write('b');
                        break;
                    case '\n':
                        out.write('\\');
                        out.write('n');
                        break;
                    case '\t':
                        out.write('\\');
                        out.write('t');
                        break;
                    case '\f':
                        out.write('\\');
                        out.write('f');
                        break;
                    case '\r':
                        out.write('\\');
                        out.write('r');
                        break;
                    default:
                        if (ch > 0xf) {
                            out.write("\\u00" + hex(ch));
                        } else {
                            out.write("\\u000" + hex(ch));
                        }
                        break;
                }
            } else {
                switch (ch) {
                    case '\'':
                        if (escapeSingleQuote) {
                            out.write('\\');
                        }
                        out.write('\'');
                        break;
                    case '"':
                        out.write('\\');
                        out.write('"');
                        break;
                    case '\\':
                        out.write('\\');
                        out.write('\\');
                        break;
                    case '/':
                        if (escapeForwardSlash) {
                            out.write('\\');
                        }
                        out.write('/');
                        break;
                    default:
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