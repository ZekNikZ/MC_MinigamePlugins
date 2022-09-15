package io.zkz.mc.minigameplugins.gametools.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

public class GTMiniMessage {
    public static final MiniMessage MM = MiniMessage.builder()
        .tags(
            TagResolver.builder()
                .resolver(StandardTags.defaults())
                .build()
        )
        .build();
}
