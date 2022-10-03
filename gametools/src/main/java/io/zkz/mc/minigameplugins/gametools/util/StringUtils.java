package io.zkz.mc.minigameplugins.gametools.util;

import io.zkz.mc.minigameplugins.gametools.GameToolsPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StringUtils {
    private StringUtils() {
    }

    private static final int[] characterWidths = new int[65536];

    public static void init(GameToolsPlugin plugin) {
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(plugin.getResourceAsStream("font-data.txt")))) {
                for (int i = 0; i < characterWidths.length; ++i) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    characterWidths[i] = Integer.parseInt(line.split(" : ")[1]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getStringWidth(String s) {
        return s.chars().map(i -> characterWidths[i] - 1).sum() // character widths
            + (s.length() - 1); // space in between characters
    }

    private static final String POSITIVE_CHARS = "\uF831\uF832\uF833\uF834\uF835\uF836\uF837\uF838\uF839\uF83A\uF83B\uF83C\uF83D\uF83E\uF83F";
    private static final String NEGATIVE_CHARS = "\uF811\uF812\uF813\uF814\uF815\uF816\uF817\uF818\uF819\uF81A\uF81B\uF81C\uF81D\uF81E\uF81F";
    private static final int[] widths = new int[]{
        1, 2, 3, 4, 5, 6, 7, 8, 16, 32, 64, 128, 256, 512, 1024
    };

    public static String getStringPadding(int width) {
        if (width == 0) {
            return "";
        }

        String baseString;
        if (width > 0) {
            baseString = POSITIVE_CHARS;
        } else {
            baseString = NEGATIVE_CHARS;
            width = -width;
        }

        StringBuilder res = new StringBuilder();
        for (int i = widths.length - 1; width > 0; ) {
            if (width >= widths[i]) {
                res.append(baseString.charAt(i));
                width -= widths[i];
            } else {
                --i; // NOSONAR java:127
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
