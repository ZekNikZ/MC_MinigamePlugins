package io.zkz.mc.minigameplugins.lobby;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import io.zkz.mc.minigameplugins.gametools.service.PluginService;
import io.zkz.mc.minigameplugins.gametools.worldedit.SchematicService;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpinnerService extends PluginService<LobbyPlugin> {
    private static final SpinnerService INSTANCE = new SpinnerService();
    private SpinnerTask spinner;

    public static SpinnerService getInstance() {
        return INSTANCE;
    }

    private List<TournamentManager.MinigameData> minigames;

    @Override
    protected void setup() {
        String[] colors = {"black", "red", "green"};
        int numSlices = 6;
        for (int i = 0; i < numSlices; i++) {
            for (String color : colors) {
                SchematicService.getInstance().preloadSchematic(i + "_" + color, SpinnerService.class.getResourceAsStream("/schematics/" + i + "_" + color + ".schem"));
            }
        }
    }

    public Location spinnerLocation() {
        return BukkitAdapter.adapt(Bukkit.getWorld("world"), Constants.SPINNER_LOCATION);
    }

    public void startSpinner() {
        this.resetSpinner();

        this.spinner = new SpinnerTask(
            IntStream.range(0, minigames.size()).filter(i -> minigames.get(i).selected()).boxed().collect(Collectors.toSet()),
            i -> TournamentManager.getInstance().chooseNextMinigame(this.minigames.get(i))
        );
        this.spinner.start(this.getPlugin());
    }

    public void pickSpinnerResult(int val) {
        this.spinner.pickResult(val);
    }

    public void resetSpinner() {
        this.minigames = TournamentManager.getInstance().getMinigames();

        for (int i = 0; i < 6; i++) {
            if (minigames.get(i).selected()) {
                SchematicService.getInstance().placeSchematic(i + "_red", SpinnerService.getInstance().spinnerLocation(), true);
            } else {
                SchematicService.getInstance().placeSchematic(i + "_black", SpinnerService.getInstance().spinnerLocation(), true);
            }
        }
    }
}
