package io.zkz.mc.minigameplugins.gametools.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class ComponentUtils {
    public static Component join(Component joiner, Iterable<? extends Component> args) {
        TextComponent.Builder res = Component.text();

        boolean first = true;
        for (Component comp : args) {
            if (!first) {
                res.append(joiner);
            }

            res.append(comp);
            first = false;
        }

        return res.build();
    }
}
