package dev.hado.fx;

/**
 * The vocabulary of cinematic hits. The client maps each kind to a layer
 * recipe, so adding a new weapon never means touching the renderer.
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
    TITLE;

    public boolean wantsHitStop() {
        return this == IMPACT || this == SLASH || this == ORBITAL || this == PARRY || this == DRILL;
    }

    public boolean wantsSpeedLines() {
        return this == SLASH || this == BARRAGE || this == ORBITAL;
    }
}
