package io.zkz.mc.minigameplugins.gametools.util;

import com.google.common.base.CharMatcher;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.map.MinecraftFont;

public class StringUtils {
    private static final MinecraftFont minecraftFont = new MinecraftFont();
    private static final String fontChars = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz{|}~\u007fÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƑáíóúñÑªº¿®¬½¼¡«»";

    private static final String dirtyChars = "\u2666\u2605\u2620\u24d0\u24d1\u24d2\u24d3\u24d4\u24d5\u24d6\u24d7\u24d8\u24d9\u24da\u24db\u24dc\u24dd\u24de\u24df\u24e0\u24e1\u24e2\u24e3\u24e4\u24e5\u24e6\u24e7\u24e8\u24e9\u24b6\u24b7\u24b8\u24b9\u24ba\u24bb\u24bc\u24bd\u24be\u24bf\u24c0\u24c1\u24c2\u24c3\u24c4\u24c5\u24c6\u24c7\u24c8\u24c9\u24ca\u24cb\u24cc\u24cd\u24ce\u24cf";
    private static final int[] dirtyWidths = new int[]{5, 7, 7, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,};

    public static int getStringWidth(String s) {
        String cleanString = CharMatcher.anyOf(dirtyChars).removeFrom(s);
        String dirtyString = CharMatcher.anyOf(dirtyChars).retainFrom(s);
        return minecraftFont.getWidth(cleanString + ChatColor.RESET) + getDirtyStringWidth(dirtyString);
    }

    private static int getDirtyStringWidth(String dirtyString) {
        return dirtyString.chars().mapToObj(i -> (char) i).mapToInt(c -> dirtyWidths[dirtyChars.indexOf(c)]).sum();
    }

    private static final int[] widths = new int[]{
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
        for (int i = widths.length - 1; width > 0; ) {
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
