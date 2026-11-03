package dev.hado.client.fx;

import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;

/**
 * One cinematic hit in flight. Mutable and pooled: the engine never allocates in
 * the render loop, it just rewrites fields on a recycled instance.
 */
public final class ScreenFx {
    public FxKind kind = FxKind.IMPACT;
    public FxStyle style = FxStyle.SHONEN;
    /** 0.5 = restrained, 1.5 = full send. Scales duration, shake and alpha. */
    public float intensity = 1.0f;
    public long startNanos;
    public long durationMs = 400;
    public int seed;
    /** Impact point in scaled screen pixels. */
    public float screenX;
    public float screenY;
    /** Null for hits that do not warrant a title card. */
    public String title;

    public static ScreenFx obtain() {
        return new ScreenFx();
    }

    public ScreenFx reset() {
        title = null;
        return this;
    }

    public float progress(long nowNanos) {
        long elapsed = (nowNanos - startNanos) / 1_000_000L;
        return Math.min(1.0f, Math.max(0.0f, (float) elapsed / durationMs));
    }

    public boolean expired(long nowNanos) {
        return (nowNanos - startNanos) / 1_000_000L >= durationMs;
    }
}
