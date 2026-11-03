package dev.hado.fx;

/**
 * Colour identity. One baked black-and-white impact plate is tinted per style at
 * draw time, so three distinct looks cost exactly one texture set.
 */
public enum FxStyle {
    /** Ink black on paper white, teal accent. */
    SHONEN(0xFFFFFFFF, 0xFF4EE8D2, 0xFFFFFFFF),
    /** Cursed energy: violet-black voids, magenta accent. */
    CURSED(0xFF8A4BE0, 0xFFE24BC0, 0xFFD8B8FF),
    /** Orbital: cyan-white with amber accent. */
    SCIFI(0xFF7FE9FF, 0xFFFF9A3C, 0xFFEAFBFF);

    public final int ink;
    public final int accent;
    public final int hot;

    FxStyle(int ink, int accent, int hot) {
        this.ink = ink;
        this.accent = accent;
        this.hot = hot;
    }

    public static FxStyle byId(int id) {
        FxStyle[] values = values();
        return values[Math.floorMod(id, values.length)];
    }
}
