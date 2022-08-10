package io.zkz.mc.minigameplugins.bingo.card;

import io.zkz.mc.minigameplugins.bingo.map.BingoCardMap;
import io.zkz.mc.minigameplugins.gametools.util.ListUtils;
import org.bukkit.Material;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BingoCard {
    private final List<Material> items;

    public BingoCard() {
        this.items = ListUtils.of(Collections.nCopies(25, Material.AIR));
    }

    public BingoCard(List<String> items) {
        this();
        for (int i = 0; i < items.size() && i < 25; i++) {
            this.items.set(i, Material.matchMaterial(items.get(i)));
        }
    }

    public List<Material> getItems() {
        return ListUtils.ofImmutable(this.items);
    }

    public void setItem(int index, Material item) {
        this.items.set(index, item);
        BingoCardMap.markDirty();
    }

    public void randomizeOrder() {
        Collections.shuffle(this.items);
        BingoCardMap.markDirty();
    }

    public void randomizeItems() {
        // TODO: fix this
        AtomicInteger i = new AtomicInteger();
        BingoItem.itemGroups().forEach((group, list) -> {
            List<BingoItem> l = ListUtils.of(list);
            Collections.shuffle(l);
            for (int j = 0; j < group.getDefaultCount(); j++) {
                this.setItem(i.getAndIncrement(), l.get(j).getMaterial());
            }
        });
        this.randomizeOrder();
    }

    public List<String> toJSON() {
        return this.getItems().stream().map(material -> material.getKey().toString()).toList();
    }
}
