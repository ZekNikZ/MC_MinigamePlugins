package io.zkz.mc.minigameplugins.survivalgames;

import io.zkz.mc.minigameplugins.gametools.resourcepack.ResourceManager;

import java.util.ArrayList;
import java.util.List;

public class ResourceAssets {
    public static final List<Character> SLIDES = new ArrayList<>();

    static {
        addRuleSlide('\uE301');
        addRuleSlide('\uE302');
        addRuleSlide('\uE303');
        addRuleSlide('\uE304');
        addRuleSlide('\uE305');
        addRuleSlide('\uE306');
        addRuleSlide('\uE307');
    }

    public static void main(String[] args) {
        for (int i = 0; i < SLIDES.size(); i++) {
            ResourceManager.addCustomCharacterImage(SLIDES.get(i), ResourceAssets.class.getResourceAsStream("/rules0" + (i + 1) + ".png"), 200, 200);
        }
        ResourceManager.addItemTexture("emerald", ResourceAssets.class.getResourceAsStream("/emerald.png"));
        ResourceManager.addMiscResource("assets/minecraft/lang/en_us.json", ResourceAssets.class.getResourceAsStream("/en_us.json"));
    }

    private static void addRuleSlide(char c) {
        SLIDES.add(c);
    }
}
