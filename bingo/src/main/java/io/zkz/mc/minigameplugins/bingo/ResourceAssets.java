package io.zkz.mc.minigameplugins.bingo;

import io.zkz.mc.minigameplugins.gametools.resourcepack.ResourceManager;

import java.util.ArrayList;
import java.util.List;

public class ResourceAssets {
    public static final List<Character> SLIDES = new ArrayList<>();

    static {
        addRuleSlide('\uE200');
        addRuleSlide('\uE201');
        addRuleSlide('\uE202');
        addRuleSlide('\uE203');
        addRuleSlide('\uE204');
        addRuleSlide('\uE205');
        addRuleSlide('\uE206');
        addRuleSlide('\uE207');
        addRuleSlide('\uE208');
    }

    public static void main(String[] args) {
        for (int i = 0; i < SLIDES.size(); i++) {
            ResourceManager.addCustomCharacterImage(SLIDES.get(i), ResourceAssets.class.getResourceAsStream("/rules/bingo_0" + i + ".png"), 200, 200);
        }
    }

    private static void addRuleSlide(char c) {
        SLIDES.add(c);
    }
}
