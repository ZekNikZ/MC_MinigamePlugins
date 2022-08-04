package io.zkz.mc.minigameplugins.gametools.util;

import com.google.common.base.CharMatcher;
import org.bukkit.map.MinecraftFont;

public class StringUtils {
    private static final MinecraftFont minecraftFont = new MinecraftFont();
    private static final String fontChars = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz{|}~\u007fÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƑáíóúñÑªº¿®¬½¼¡«»";

    private static final String dirtyChars = "\u2666\u2605\u2620";
    private static final int[] dirtyWidths = new int[]{5, 7, 7};

    public static int getStringWidth(String s) {
        String cleanString = CharMatcher.anyOf(fontChars).removeFrom(s);
        String dirtyString = CharMatcher.anyOf(fontChars).retainFrom(s);
        return minecraftFont.getWidth(cleanString) + getDirtyStringWidth(dirtyString);
    }

    private static int getDirtyStringWidth(String dirtyString) {
        return dirtyString.chars().mapToObj(i -> (char) i).mapToInt(c -> dirtyWidths[dirtyChars.indexOf(c)]).sum();
    }

    private static final String negativeChars = "\uF811\uF812\uF813\uF814\uF815\uF816\uF817\uF818\uF819\uF81A\uF81B\uF81C\uF81D\uF81E\uF81F";
    private static final String positiveChars = "\uF831\uF832\uF833\uF834\uF835\uF836\uF837\uF838\uF839\uF83A\uF83B\uF83C\uF83D\uF83E\uF83F";
    private static final int[] widths = new int[] {
        1, 2, 3, 4, 5, 6, 7, 8, 16, 32, 64, 128, 256, 512, 1024
    };

    public static String getStringPadding(int width) {
        char baseChar;
        if (width > 0) {
            baseChar = '\uF831';
        } else {
            baseChar = '\uF811';
            width = -width;
        }

        StringBuilder res = new StringBuilder();
        for (int i = widths.length - 1; width > 0;) {
            if (width >= widths[i]) {
                res.append(baseChar + i);
                width -= widths[i];
            } else {
                --i;
            }
        }

        return res.toString();
    }

    public static String padOnLeftWithPixels(String s, int pixelWidth) {
        int width = getStringWidth(s);
        if (width >= pixelWidth) {
            return s;
        }
        String padding = getStringPadding(pixelWidth - width);
        return padding + s;
    }

    public static String padOnRightWithPixels(String s, int pixelWidth) {
        int width = getStringWidth(s);
        if (width >= pixelWidth) {
            return s;
        }
        String padding = getStringPadding(pixelWidth - width);
        return s + padding;
    }
}
