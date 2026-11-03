package dev.hado.marker;

import net.minecraft.entity.player.PlayerEntity;

/**
 * Small shared control API for the marker remote and the marker screen.
 *
 * <p>Kept intentionally small and lean: one active marker per player,
 * no history, no cross-player sharing, no world tracking.
 */
public final class HadoMarkerControl {
    private HadoMarkerControl() {
    }

    public static HadoMarker current(PlayerEntity player) {
        return HadoMarkerStorage.get(player);
    }

    public static HadoMarker ensure(PlayerEntity player) {
        return HadoMarkerStorage.getOrCreate(player);
    }

    public static void clear(PlayerEntity player) {
        HadoMarkerStorage.remove(player);
    }
}
