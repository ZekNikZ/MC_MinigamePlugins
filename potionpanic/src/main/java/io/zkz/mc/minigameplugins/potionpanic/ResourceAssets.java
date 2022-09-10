package io.zkz.mc.minigameplugins.potionpanic;

import io.zkz.mc.minigameplugins.gametools.resourcepack.ResourceManager;

import java.util.ArrayList;
import java.util.List;

public class ResourceAssets {
    public static final List<Character> SLIDES = new ArrayList<>();

    static {
        addRuleSlide('\uE700');
        addRuleSlide('\uE701');
        addRuleSlide('\uE702');
        addRuleSlide('\uE703');
        addRuleSlide('\uE704');
        addRuleSlide('\uE705');
    }

    public static void main(String[] args) {
        for (int i = 0; i < SLIDES.size(); i++) {
            ResourceManager.addCustomCharacterImage(SLIDES.get(i), ResourceAssets.class.getResourceAsStream("/rules/potionpanic_0" + i + ".png"), 200, 200);
        }
    }

    private static void addRuleSlide(char c) {
        SLIDES.add(c);
    }
}
