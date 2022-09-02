package io.zkz.mc.minigameplugins.battlebox;

import io.zkz.mc.minigameplugins.gametools.resourcepack.ResourceManager;

import java.util.ArrayList;
import java.util.List;

public class ResourceAssets {
    public static final List<Character> SLIDES = new ArrayList<>();

    public static void main(String[] args) {
        addRuleSlide(ResourceManager.addCustomCharacterImage('\uE700', ResourceAssets.class.getResourceAsStream("/testinstructions.png"), 200, 200));
        addRuleSlide(ResourceManager.addCustomCharacterImage('\uE701', ResourceAssets.class.getResourceAsStream("/testinstructions2.png"), 200, 200));
    }

    private static void addRuleSlide(char c) {
        SLIDES.add(c);
    }
}
