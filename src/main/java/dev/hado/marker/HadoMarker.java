package dev.hado.marker;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Lightweight per-player marker record.
 *
 * <p>A marker is a saved target point plus a small recent-event note for the
 * holder. It is session-only and deliberately small for this slice: no chunk
 * snapshots, no block-region history, no world rollback. The "restore" action is a
 * bounded cue: a rewind-style FX feel, a teleport of the holder to the marker
 * target, and a short last-event note echoed back through the overlay. It does not
 * undo the rest of the world.
 *
 * <p>One active marker per player in this slice. There is no marker history and no
 * cross-player markers.
 */
public final class HadoMarker {
    private static final int MAX_EVENT_NOTE_LENGTH = 64;

    private final PlayerEntity owner;
    private Vec3d target;
    private String eventNote;
    private long setTime;

    public HadoMarker(PlayerEntity owner) {
        this.owner = owner;
        clear();
    }

    public void set(Vec3d target, String eventNote) {
        this.target = target == null ? owner.getPos() : target;
        this.eventNote = (eventNote == null) ? "" : eventNote.substring(0, Math.min(eventNote.length(), MAX_EVENT_NOTE_LENGTH));
        this.setTime = System.currentTimeMillis();
    }

    public void clear() {
        this.target = owner.getPos();
        this.eventNote = "";
        this.setTime = 0L;
    }

    public boolean isSet() {
        return target != null && setTime > 0L;
    }

    public Vec3d target() {
        return target;
    }

    public String eventNote() {
        return eventNote;
    }

    public long setTime() {
        return setTime;
    }

    public PlayerEntity owner() {
        return owner;
    }

    public String summary() {
        if (!isSet()) {
            return "no marker set";
        }
        return String.format("marker %s at %.1f, %.1f, %.1f",
                noteOrTag(), target.x, target.y, target.z);
    }

    private String noteOrTag() {
        if (eventNote.isEmpty()) {
            return "set";
        }
        return "noted";
    }
}
