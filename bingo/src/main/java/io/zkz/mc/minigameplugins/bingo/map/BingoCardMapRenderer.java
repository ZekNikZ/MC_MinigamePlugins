package io.zkz.mc.minigameplugins.bingo.map;

import io.zkz.mc.minigameplugins.bingo.BingoRound;
import io.zkz.mc.minigameplugins.bingo.card.BingoCard;
import io.zkz.mc.minigameplugins.gametools.teams.GameTeam;
import io.zkz.mc.minigameplugins.minigamemanager.service.MinigameService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.stream.Collectors;

public class BingoCardMapRenderer extends MapRenderer {
    private static final int IMAGE_START_X = 3;
    private static final int IMAGE_START_Y = 3;
    private static final int IMAGE_MARGIN = 3;
    private static final int IMAGE_PADDING = 3;
    private static final int IMAGE_WIDTH = 16;
    private static final BufferedImage BARRIER = MapImageLoader.get("barrier");

    private final Map<UUID, Boolean> isClean = new HashMap<>();

    public BingoCardMapRenderer() {
        super(true);
    }

    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
        if (Optional.ofNullable(this.isClean.get(player.getUniqueId())).orElse(false)) {
            return;
        }

        // Background
        MapUtils.fill(canvas, MapPalette.DARK_GRAY);

        // Items
        BingoRound currentRound = ((BingoRound) MinigameService.getInstance().getCurrentRound());
        List<Material> items = Optional.ofNullable(currentRound).map(BingoRound::getCard).map(BingoCard::getItems).orElse(null);
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                int i = x + y * 5;

                // Get item image
                BufferedImage image = null;
                if (items != null) {
                    if (
                        currentRound.getPointsForItem(items.get(i)) == 0) {
                        image = MapImageLoader.getGrayscale(items.get(i).getKey().getKey());
                    } else {
                        image = MapImageLoader.get(items.get(i).getKey().getKey());
                    }
                }
                if (image == null) {
                    image = BARRIER;
                }

                int startX = IMAGE_START_X + x * (IMAGE_WIDTH + IMAGE_PADDING * 2 + IMAGE_MARGIN);
                int startY = IMAGE_START_Y + y * (IMAGE_WIDTH + IMAGE_PADDING * 2 + IMAGE_MARGIN);

                // Draw slot
                MapUtils.fill(
                    canvas,
                    startX,
                    startY,
                    startX + IMAGE_WIDTH + 2 * IMAGE_PADDING - 1,
                    startY + IMAGE_WIDTH + 2 * IMAGE_PADDING - 1,
                    MapPalette.WHITE
                );

                // Draw image
                MapUtils.drawImageWithTransparency(
                    canvas,
                    image,
                    startX + IMAGE_PADDING,
                    startY + IMAGE_PADDING
                );

                // Draw team collections
                if (items == null) {
                    continue;
                }
                List<Byte> colors =
                    currentRound.getCollectorsOfItem(items.get(i)).stream()
                        .map(GameTeam::getColor)
                        .map(MapPalette::matchColor)
                        .collect(Collectors.toList());
                switch (colors.size()) {
                    case 4:
                        MapUtils.fill(
                            canvas,
                            startX,
                            startY + IMAGE_PADDING + IMAGE_WIDTH + 1,
                            startX + IMAGE_PADDING + IMAGE_WIDTH + IMAGE_PADDING - 1,
                            startY + IMAGE_PADDING + IMAGE_WIDTH + IMAGE_PADDING - 1,
                            colors.get(3)
                        );
                    case 3:
                        MapUtils.fill(
                            canvas,
                            startX + IMAGE_PADDING + IMAGE_WIDTH + 1,
                            startY + IMAGE_PADDING - 1,
                            startX + IMAGE_PADDING + IMAGE_WIDTH + IMAGE_PADDING - 1,
                            startY + IMAGE_PADDING + IMAGE_WIDTH - 1 + 1,
                            colors.get(2)
                        );
                    case 2:
                        MapUtils.fill(
                            canvas,
                            startX,
                            startY + IMAGE_PADDING - 1,
                            startX + IMAGE_PADDING - 1 /* todo: remove */ - 1,
                            startY + IMAGE_PADDING + IMAGE_WIDTH - 1 + 1,
                            colors.get(1)
                        );
                    case 1:
                        MapUtils.fill(
                            canvas,
                            startX,
                            startY,
                            startX + IMAGE_PADDING + IMAGE_WIDTH + IMAGE_PADDING - 1,
                            startY + IMAGE_PADDING - 1 /* todo: remove */ - 1,
                            colors.get(0)
                        );
                    default:
                        break;
                }
            }
        }

        isClean.put(player.getUniqueId(), true);
    }

    public void markDirty() {
        this.isClean.clear();
    }
}
