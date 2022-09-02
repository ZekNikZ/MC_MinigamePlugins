package io.zkz.mc.minigameplugins.tgttos;

import io.zkz.mc.minigameplugins.gametools.resourcepack.ResourceManager;

import java.util.ArrayList;
import java.util.List;

public class ResourceAssets {
    public static final List<Character> SLIDES = new ArrayList<>();

    static {
        addRuleSlide('\uE500');
        addRuleSlide('\uE501');
        addRuleSlide('\uE502');
        addRuleSlide('\uE503');
        addRuleSlide('\uE504');
    }

    public static void main(String[] args) {
        for (int i = 0; i < SLIDES.size(); i++) {
            ResourceManager.addCustomCharacterImage(SLIDES.get(i), ResourceAssets.class.getResourceAsStream("/rules/tgttos_0" + i + ".png"), 200, 200);
        }
    }

    private static void addRuleSlide(char c) {
        SLIDES.add(c);
    }
}
