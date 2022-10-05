package io.zkz.mc.minigameplugins.tgttos.round;

import io.zkz.mc.minigameplugins.tgttos.TGTTOSRound;
import org.bukkit.entity.Player;

public class NothingRound extends TGTTOSRound {
    public NothingRound(TypedJSONObject<Object> json) {
        super(RoundType.NOTHING, json);
    }

    @Override
    protected void setupPlayerInventory(Player player) {
        player.getInventory().setHeldItemSlot(0);
    }
}
