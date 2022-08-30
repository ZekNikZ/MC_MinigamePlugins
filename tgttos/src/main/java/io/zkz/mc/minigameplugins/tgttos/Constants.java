package io.zkz.mc.minigameplugins.tgttos;

import java.util.Random;

public class Constants {
    private static final String[] DEATH_MESSAGES = new String[] {
        " decided to start over.",
        " had unexpected items in the bagging area.",
        " thought down was up",
        " got tired of winning.",
        " succumbed to the dark side.",
        " forgot the goal of the game.",
        " did not, in fact, make it to the other side.",
        " told a bad dad joke.",
        " tried to cheat.",
        " decided that they had better things to do.",
        " was too good. We had to take them down a few notches.",
        " found out what happens when the chicken does not make it across the road.",
        " didn't get the joke.",
        " believed they could fly! They couldn't.",
        " cracked under the pressure.",
        " needed some alone time.",
        " stopped to smell the roses.",
        " shoes were tied together.",
        " forgot what the goal was.",
        " died. No funny message. It was just embarrassing.",
        " left auto-jump on.",
        " yeeted themselves into the void.",
        " spent too much time on TikTok.",
        " stole candy from a baby.",
        "'s parents called them for dinner."
    };

    public static String randomDeathMessage() {
        return DEATH_MESSAGES[new Random().nextInt(DEATH_MESSAGES.length)];
    }
}
