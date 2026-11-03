package dev.hado.fx;

import net.minecraft.util.math.Vec3d;

/**
 * Cinematic kind vocabulary for the expanded arsenal.
 * <p>Each kind maps to a layer recipe in {@link dev.hado.client.fx.FxOverlay},
 * so new weapons and gadgets add rows here rather than new draw code.
 */
public enum FxKind {
    /** Standard strike: flash, ink plate, shake. */
    IMPACT,
    /** Long sweeping cut: slash plate + directional speed lines. */
    SLASH,
    /** Orbital beam: white-out, rings, heavy shake, long hit-stop. */
    ORBITAL,
    /** Inward implosion: vignette crush + inverted plate. */
    IMPLOSION,
    /** Rapid projectiles: speed lines only, no hit-stop (keeps barrages smooth). */
    BARRAGE,
    /** Perfect guard: hex grid + short freeze. */
    PARRY,
    /** Drill contact: shake-heavy, no white-out. */
    DRILL,
    /** Title card only (menu / remote demo). */
    TITLE,

    // --- marker / coordination kinds ---

    /** Counter-slash: sharp returned slash plate + decisive hit-stop. */
    SLASHBACK,
    /** Rewind pulse: soft rings + trailing afterimage spark, gentle pullback feel. */
    RETURN,
    /** Beacon pulse: soft expanding ring toward a marker target point. */
    BEACON,
    /** Thread/tether pull: paired ring + inward pull feel. */
    TETHER,
    /** Chime ring: short sustained ring, no freeze. */
    CHIME,
    /** Lantern glow: soft expanding glow, no freeze. */
    GLOW,
    /** Kite tail line: trailing line pulse, no freeze. */
    TAIL,
    /** Map sweep: flat plate sweep, no heavy freeze. */
    SWEEP,
    /** Anchor stamp: short ground imprint + short freeze. */
    STAMP,
    /** Scroll reveal: page-turn title card + short soft ring. */
    REVEAL,
    /** Lens flare: sharp lens flare + brief flash, no freeze. */
    LENS;

    public boolean wantsHitStop() {
        return this == IMPACT || this == SLASH || this == ORBITAL
                || this == PARRY || this == DRILL || this == SLASHBACK || this == STAMP;
    }

    public boolean wantsSpeedLines() {
        return this == SLASH || this == BARRAGE || this == ORBITAL;
    }

    public boolean wantsRing() {
        return this == RETURN || this == ORBITAL
                || this == IMPLOSION || this == BEACON || this == TETHER || this == TAIL || this == CHIME || this == GLOW || this == REVEAL || this == SWEEP || this == STAMP;
    }

    public boolean wantsTitleCard() {
        return this == BEACON || this == TETHER || this == CHIME || this == GLOW || this == TAIL
                || this == SWEEP || this == STAMP || this == REVEAL || this == LENS;
    }

    public boolean isMarkerKind() {
        return this == RETURN || this == BEACON || this == TETHER || this == TAIL || this == CHIME || this == GLOW || this == REVEAL || this == SWEEP || this == STAMP;
    }

    /** Marker-ish kinds that get a ring + an optional title card cue. */
    public static boolean isMarkerKind(FxKind kind) {
        return kind != null && kind.isMarkerKind();
    }

    public static Vec3d at(Vec3d a, Vec3d b) {
        return a != null ? a : b;
    }
}
