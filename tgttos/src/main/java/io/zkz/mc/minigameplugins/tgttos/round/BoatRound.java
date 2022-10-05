package io.zkz.mc.minigameplugins.tgttos.round;

import io.zkz.mc.minigameplugins.gametools.util.ISB;
import io.zkz.mc.minigameplugins.tgttos.TGTTOSRound;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BoatRound extends TGTTOSRound {
    public BoatRound(TypedJSONObject<Object> json) {
        super(RoundType.BOAT, json);
    }

    @Override
    protected void setupPlayerInventory(Player player) {
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setItem(0, ISB.stack(Material.OAK_BOAT));
    }
}
