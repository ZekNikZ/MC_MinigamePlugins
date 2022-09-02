package io.zkz.mc.minigameplugins.battlebox.round;

import io.zkz.mc.minigameplugins.battlebox.TGTTOSRound;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
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
