package dev.hado.marker;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session-persistent storage for one active marker per player.
 *
 * <p>Keep it small on purpose: one marker per player, no history, no
 * cross-player sharing, no world-state tracking. The restore path only re-cues
 * the holder and teleports them to the stored target.
 */
public final class HadoMarkerStorage {
    private static final Map<UUID, HadoMarker> MARKERS = new ConcurrentHashMap<>();

    private HadoMarkerStorage() {
    }

    public static HadoMarker getOrCreate(PlayerEntity player) {
        return MARKERS.computeIfAbsent(player.getUuid(), k -> new HadoMarker(player));
    }

    public static HadoMarker get(PlayerEntity player) {
        return MARKERS.get(player.getUuid());
    }

    public static void put(PlayerEntity player, HadoMarker marker) {
        MARKERS.put(player.getUuid(), marker);
    }

    public static void remove(PlayerEntity player) {
        MARKERS.remove(player.getUuid());
    }
}
