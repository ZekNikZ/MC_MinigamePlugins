package io.zkz.mc.minigameplugins.gametools.util;

import com.mojang.brigadier.context.CommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

public class ComponentUtils {
    private ComponentUtils() {}

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

    private static class ComponentJoiner {
        private final List<Component> els = new ArrayList<>();
        private final Component joiner;

        private ComponentJoiner(Component joiner) {
            this.joiner = joiner;
        }

        public void add(Component component) {
            this.els.add(component);
        }

        public ComponentJoiner merge(ComponentJoiner other) {
            this.els.addAll(other.els);
            return this;
        }

        public Component build() {
            return join(this.joiner, this.els);
        }
    }

    public static Collector<Component, ?, Component> joining(Component joiner) {
        return Collector.of(
            () -> new ComponentJoiner(joiner),
            ComponentJoiner::add,
            ComponentJoiner::merge,
            ComponentJoiner::build
        );
    }

    public static Component extractArgument(CommandContext<CommandSourceStack> cmd, String name) {
        return GsonComponentSerializer.gson().deserialize(net.minecraft.network.chat.Component.Serializer.toJson(ComponentArgument.getComponent(cmd, name)));
    }
}
