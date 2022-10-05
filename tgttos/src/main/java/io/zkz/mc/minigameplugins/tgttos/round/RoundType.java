package io.zkz.mc.minigameplugins.tgttos.round;

import io.zkz.mc.minigameplugins.tgttos.TGTTOSRound;

import java.util.function.Function;

public enum RoundType {
    NOTHING(NothingRound::new),
    BOAT(BoatRound::new),
    BUILD(BuildRound::new),
    GLIDE(GlideRound::new);

    private final Function<TypedJSONObject<Object>, TGTTOSRound> creator;

    RoundType(Function<TypedJSONObject<Object>, TGTTOSRound> creator) {
        this.creator = creator;
    }

    public TGTTOSRound create(TypedJSONObject<Object> json) {
        return this.creator.apply(json);
    }
}
