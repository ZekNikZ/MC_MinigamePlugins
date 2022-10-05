package io.zkz.mc.minigameplugins.gametools.resourcepack;

import java.util.List;

record FontProvider(String type, String file, int ascent, int height, List<String> chars) {
}

public record FontData(List<FontProvider> providers) {
}
