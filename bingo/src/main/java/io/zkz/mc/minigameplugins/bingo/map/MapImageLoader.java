package io.zkz.mc.minigameplugins.bingo.map;

import io.zkz.mc.minigameplugins.bingo.card.BingoItem;
import org.bukkit.Material;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class MapImageLoader {
    private static final String IMAGE_FOLDER = "/images/";
    private static final Map<String, BufferedImage> IMAGES;
    private static final Map<String, BufferedImage> GRAYSCALE_IMAGES;

    static {
        Map<String, BufferedImage> images = new HashMap<>();

        List<Material> materials = Arrays.stream(BingoItem.values()).map(BingoItem::getMaterial).collect(Collectors.toCollection(ArrayList::new));
        materials.add(Material.BARRIER);
        materials.sort(Comparator.comparing(material -> material.getKey().getKey()));
        for (Material mat : materials) {
            if (mat.isLegacy()) {
                continue;
            }

            String key = mat.getKey().getKey();

            InputStream in = MapImageLoader.class.getResourceAsStream(IMAGE_FOLDER + key + ".png");
            if (in == null) {
                System.err.println("Could not find image for key " + key);
                continue;
            }

            try {
                BufferedImage image = ImageIO.read(in);
                if (image == null) {
                    System.err.println("Image for key " + key + " could not be read for some reason");
                } else {
                    images.put(key, image);
                    System.out.println("Loaded image for key " + key);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Images
        IMAGES = Collections.unmodifiableMap(images);

        // Grayscale images
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        GRAYSCALE_IMAGES = Collections.unmodifiableMap(
            images.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> {
                        BufferedImage image = entry.getValue();
                        BufferedImage result = new BufferedImage(
                            image.getWidth(),
                            image.getHeight(),
                            BufferedImage.TYPE_INT_ARGB);
                        op.filter(image, result);
                        return result;
                    }
                ))
        );
    }

    public static BufferedImage get(String key) {
        return IMAGES.get(key);
    }

    public static BufferedImage getGrayscale(String key) {
        return GRAYSCALE_IMAGES.get(key);
    }
}
