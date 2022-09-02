package io.zkz.mc.minigameplugins.guessthebuild;

import io.zkz.mc.minigameplugins.gametools.resourcepack.ResourceManager;

import java.util.ArrayList;
import java.util.List;

public class ResourceAssets {
    public static final List<Character> SLIDES = new ArrayList<>();

    static {
        addRuleSlide('\uE300');
        addRuleSlide('\uE301');
        addRuleSlide('\uE302');
        addRuleSlide('\uE303');
        addRuleSlide('\uE304');
        addRuleSlide('\uE305');
    }

    public static void main(String[] args) {
        for (int i = 0; i < SLIDES.size(); i++) {
            ResourceManager.addCustomCharacterImage(SLIDES.get(i), ResourceAssets.class.getResourceAsStream("/rules/guessthebuild_0" + i + ".png"), 200, 200);
        }
    }

    private static void addRuleSlide(char c) {
        SLIDES.add(c);
    }
}
