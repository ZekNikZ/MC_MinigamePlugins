package io.zkz.mc.minigameplugins.guessthebuild;

import io.zkz.mc.minigameplugins.gametools.resourcepack.ResourceManager;

import java.util.ArrayList;
import java.util.List;

public class ResourceAssets {
    public static final List<Character> SLIDES = new ArrayList<>();

    public static void main(String[] args) {
        addRuleSlide(ResourceManager.addCustomCharacterImage('\uE600', ResourceAssets.class.getResourceAsStream("/testinstructions.png"), 200, 200));
        addRuleSlide(ResourceManager.addCustomCharacterImage('\uE601', ResourceAssets.class.getResourceAsStream("/testinstructions2.png"), 200, 200));
    }

    private static void addRuleSlide(char c) {
        SLIDES.add(c);
    }
}
