package io.zkz.mc.minigameplugins.gametools.util;

import java.util.Objects;

public record Pair<K, V>(K key, V value) {
    public K first() {
        return this.key;
    }

    public V second() {
        return this.value;
    }

    public boolean eitherMatch(Object val) {
        return Objects.equals(this.key, val) || Objects.equals(this.value, val);
    }
}