package io.zkz.mc.minigameplugins.dev.survivalgames;

import com.sk89q.worldedit.math.BlockVector3;

import java.util.Objects;

public final class SGChest {
    private BlockVector3 pos;
    private String lootTable;

    SGChest(BlockVector3 pos, String lootTable) {
        this.pos = pos;
        this.lootTable = lootTable;
    }

    public BlockVector3 pos() {
        return pos;
    }

    public String lootTable() {
        return lootTable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SGChest) obj;
        return Objects.equals(this.pos, that.pos) &&
            Objects.equals(this.lootTable, that.lootTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, lootTable);
    }

    @Override
    public String toString() {
        return "SGChest[" +
            "pos=" + pos + ", " +
            "lootTable=" + lootTable + ']';
    }

    public void setPos(BlockVector3 pos) {
        this.pos = pos;
    }

    public void setLootTable(String lootTable) {
        this.lootTable = lootTable;
    }
}