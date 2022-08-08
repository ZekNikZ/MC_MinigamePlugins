package io.zkz.mc.minigameplugins.bingo.map;

import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

public class MapUtils {
    public static void drawImageWithTransparency(MapCanvas canvas, BufferedImage img, int posX, int posY) {
        try {
            int height = img.getHeight();
            int width = img.getWidth();
            int i = 0;
            int[] pixels = new int[width * height];

            PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height, pixels, 0, width);

            pg.grabPixels();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color c = new Color(pixels[i], true);

                    if (c.getAlpha() > 0) {
                        int red = c.getRed();
                        int green = c.getGreen();
                        int blue = c.getBlue();

                        canvas.setPixel(posX + x, posY + y, MapPalette.matchColor(red, green, blue));
                    }

                    i++;
                }
            }
        } catch (InterruptedException ignored) {
        }
    }

    public static void fill(MapCanvas canvas, int x1, int y1, int x2, int y2, byte color) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                canvas.setPixel(x, y, color);
            }
        }
    }

    public static void fill(MapCanvas canvas, byte color) {
        fill(canvas, 0, 0, 127, 127, color);
    }
}
