package io.zkz.mc.minigameplugins.survivalgames;

import io.zkz.mc.minigameplugins.gametools.resourcepack.ResourceManager;

import java.util.ArrayList;
import java.util.List;

public class ResourceAssets {
    public static final List<Character> SLIDES = new ArrayList<>();

    static {
        addRuleSlide('\uE400');
        addRuleSlide('\uE401');
        addRuleSlide('\uE402');
        addRuleSlide('\uE403');
        addRuleSlide('\uE404');
        addRuleSlide('\uE405');
        addRuleSlide('\uE406');
        addRuleSlide('\uE407');
    }

    public static void main(String[] args) {
        for (int i = 0; i < SLIDES.size(); i++) {
            ResourceManager.addCustomCharacterImage(SLIDES.get(i), ResourceAssets.class.getResourceAsStream("/rules/survivalgames_0" + i + ".png"), 200, 200);
        }
    }

    private static void addRuleSlide(char c) {
        SLIDES.add(c);
    }
}
