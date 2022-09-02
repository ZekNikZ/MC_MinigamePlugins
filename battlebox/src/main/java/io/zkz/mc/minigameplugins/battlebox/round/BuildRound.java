package io.zkz.mc.minigameplugins.battlebox.round;

import io.zkz.mc.minigameplugins.battlebox.TGTTOSRound;
import io.zkz.mc.minigameplugins.gametools.data.json.TypedJSONObject;
import io.zkz.mc.minigameplugins.gametools.teams.TeamService;
import io.zkz.mc.minigameplugins.gametools.util.ISB;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class BuildRound extends TGTTOSRound {
    public BuildRound(TypedJSONObject<Object> json) {
        super(RoundType.BUILD, json);
    }

    @Override
    protected void setupPlayerInventory(Player player) {
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setItem(0, ISB.material(Material.SHEARS).unbreakable().build());
        player.getInventory().setItem(EquipmentSlot.OFF_HAND, ISB.stack(TeamService.getInstance().getTeamOfPlayer(player).getWoolColor(), 64));
    }
}
