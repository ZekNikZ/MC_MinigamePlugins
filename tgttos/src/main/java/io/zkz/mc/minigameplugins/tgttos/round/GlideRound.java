package io.zkz.mc.minigameplugins.tgttos.round;

import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import io.zkz.mc.minigameplugins.tgttos.TGTTOSRound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class GlideRound extends TGTTOSRound {
    public GlideRound(TypedJSONObject<Object> json) {
        super(RoundType.GLIDE, json);
    }

    @Override
    protected void setupPlayerInventory(Player player) {
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setItem(0, ISB.material(Material.SHEARS).unbreakable().build());
        player.getInventory().setItem(EquipmentSlot.OFF_HAND, ISB.stack(TeamService.getInstance().getTeamOfPlayer(player).getWoolColor(), 64));
        player.getInventory().setItem(EquipmentSlot.CHEST, ISB.material(Material.ELYTRA).unbreakable().build());
    }
}
