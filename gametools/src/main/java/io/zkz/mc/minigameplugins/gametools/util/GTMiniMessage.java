package io.zkz.mc.minigameplugins.gametools.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

public class GTMiniMessage {
    public static final MiniMessage MM = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.defaults())
                .build()
        )
        .build();

    public static Component mmResolve(String s, final @NotNull TagResolver... tagResolvers) {
        return MM.deserialize(s, tagResolvers);
    }

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
