package io.zkz.mc.minigameplugins.gametools.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

public class GTMiniMessage {
    private GTMiniMessage() {}

    public static final MiniMessage MM = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.hoverEvent())
                .resolver(StandardTags.clickEvent())
                .resolver(StandardTags.keybind())
                .resolver(StandardTags.translatable())
                .resolver(StandardTags.insertion())
                .resolver(StandardTags.font())
                .resolver(StandardTags.decorations())
                .resolver(StandardTags.gradient())
                .resolver(StandardTags.rainbow())
                .resolver(StandardTags.reset())
                .resolver(StandardTags.newline())
                .resolver(StandardTags.transition())
                .resolver(StandardTags.selector())
                .resolver(GTColors.INSTANCE)
                .build()
        )
        .build();

    public static Component mmResolve(String s, final @NotNull TagResolver... tagResolvers) {
        return MM.deserialize(s, tagResolvers);
    }

    @SuppressWarnings("java:S1845")
    public static Component mm(String s, Component... args) {
        return mmResolve(
            s,
            IntStream.range(0, args.length)
                .mapToObj(i -> Placeholder.component("" + i, args[i]))
                .toArray(TagResolver[]::new)
        );
    }

    public static Component mmArgs(String s, Object... args) {
        return mmResolve(
            s,
            IntStream.range(0, args.length)
                .mapToObj(i -> Placeholder.unparsed("" + i, String.valueOf(args[i])))
                .toArray(TagResolver[]::new)
        );
    }
}
