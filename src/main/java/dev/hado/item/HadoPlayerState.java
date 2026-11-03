package dev.hado.item;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Two small per-player integers (FX level, style index), kept server-side so the
 * gadgets survive logging out and never trust client input.
 */
public final class HadoPlayerState {
    private static final Map<UUID, int[]> STATE = new ConcurrentHashMap<>();

    private HadoPlayerState() {
    }

    public static int get(PlayerEntity player, int slot) {
        int[] s = STATE.get(player.getUuid());
        return s == null ? 0 : s[slot];
    }

    public static void set(PlayerEntity player, int slot, int value) {
        STATE.computeIfAbsent(player.getUuid(), k -> new int[2])[slot] = value;
    }

    /** Returns the current value for the slot (before the caller advances it). */
    public static int cycle(PlayerEntity player, int slot, int modulo) {
        return Math.floorMod(get(player, slot), modulo);
    }
}
