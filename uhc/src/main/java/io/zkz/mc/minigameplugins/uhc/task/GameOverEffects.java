package io.zkz.mc.minigameplugins.uhc.task;

import io.zkz.mc.minigameplugins.gametools.util.Chat;
import io.zkz.mc.minigameplugins.gametools.util.ChatType;
import io.zkz.mc.minigameplugins.minigamemanager.minigame.MinigameService;
import io.zkz.mc.minigameplugins.minigamemanager.task.MinigameTask;
import io.zkz.mc.minigameplugins.uhc.settings.SettingsManager;
import io.zkz.mc.minigameplugins.uhc.settings.enums.TeamStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

import static io.zkz.mc.minigameplugins.gametools.util.GTMiniMessage.mm;

public class GameOverEffects extends MinigameTask {
    private int fireworks = 5;
    private final Component title, subtitle;

    public GameOverEffects() {
        super(20, 20);

        title = mm("<legacy_gold><bold>GAME OVER");
        if (SettingsManager.SETTING_TEAM_GAME.value() == TeamStatus.TEAM_GAME) {
            var team = MinigameService.getInstance().getCurrentRound().getAliveTeams().keySet().stream().findFirst();
            subtitle = team.map(gameTeam -> mm("<0> <legacy_aqua> wins !", gameTeam.getDisplayName())).orElseGet(() -> mm(""));
        } else {
            var player = MinigameService.getInstance().getCurrentRound().getOnlineAlivePlayers().stream().findFirst();
            subtitle = player.map(value -> mm("<legacy_aqua><0> wins !", value.displayName())).orElseGet(() -> mm(""));
        }

        Chat.sendMessage(Bukkit.getServer(), ChatType.GAME_INFO, mm("<legacy_gold>Game over!</legacy_gold> " + subtitle));
    }

    @Override
    public void run() {
        if (this.fireworks == 0) {
            this.cancel();
            return;
        }

        Bukkit.getServer().showTitle(Title.title(title, subtitle));

        MinigameService.getInstance().getCurrentRound().getOnlineAlivePlayers().forEach(p -> {
            //Spawn the Firework, get the FireworkMeta.
            Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();

            //Our random generator
            Random r = new Random();

            //Get the type
            FireworkEffect.Type type = FireworkEffect.Type.values()[r.nextInt(5)];

            //Get our random colours
            int r1i = r.nextInt(17) + 1;
            int r2i = r.nextInt(17) + 1;
            Color c1 = getColor(r1i);
            Color c2 = getColor(r2i);

            //Create our effect with this
            FireworkEffect effect = FireworkEffect.builder().flicker(r.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(r.nextBoolean()).build();

            //Then apply the effect to the meta
            fwm.addEffect(effect);

            //Generate some random power and set it
            int rp = r.nextInt(2) + 1;
            fwm.setPower(rp);

            //Then apply this to our rocket
            fw.setFireworkMeta(fwm);
        });

        this.fireworks--;
    }

    private Color getColor(int i) {
        Color c = null;
        if (i == 1) {
            c = Color.AQUA;
        }
        if (i == 2) {
            c = Color.BLACK;
        }
        if (i == 3) {
            c = Color.BLUE;
        }
        if (i == 4) {
            c = Color.FUCHSIA;
        }
        if (i == 5) {
            c = Color.GRAY;
        }
        if (i == 6) {
            c = Color.GREEN;
        }
        if (i == 7) {
            c = Color.LIME;
        }
        if (i == 8) {
            c = Color.MAROON;
        }
        if (i == 9) {
            c = Color.NAVY;
        }
        if (i == 10) {
            c = Color.OLIVE;
        }
        if (i == 11) {
            c = Color.ORANGE;
        }
        if (i == 12) {
            c = Color.PURPLE;
        }
        if (i == 13) {
            c = Color.RED;
        }
        if (i == 14) {
            c = Color.SILVER;
        }
        if (i == 15) {
            c = Color.TEAL;
        }
        if (i == 16) {
            c = Color.WHITE;
        }
        if (i == 17) {
            c = Color.YELLOW;
        }

        return c;
    }
}
