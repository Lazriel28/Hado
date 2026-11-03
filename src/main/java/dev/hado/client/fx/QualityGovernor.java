package dev.hado.client.fx;

/**
 * Frame-time governor. The mod looks expensive; it refuses to *be* expensive.
 *
 * <p>It measures a smoothed frame time every frame and publishes a quality tier
 * 0..3. The overlay renderer drops layers from the outside in as the tier falls,
 * so a weak GPU loses grain, then speed lines, then secondary glows — the impact
 * frame itself is the last thing to go.
 */
public final class QualityGovernor {
    private static final double EMA_ALPHA = 0.05;

    private static double smoothedFrameMs = 16.0;
    private static long lastFrameNanos;
    private static int tier = 3;
    private static int ceiling = 3;

    private QualityGovernor() {
    }

    public static void setCeiling(int value) {
        ceiling = Math.max(0, Math.min(3, value));
    }

    /** Called once per rendered frame from the HUD callback. */
    public static void sample() {
        long now = System.nanoTime();
        if (lastFrameNanos != 0L) {
            double ms = (now - lastFrameNanos) / 1_000_000.0;
            // Ignore absurd deltas (alt-tab, loading pauses) so they don't tank quality.
            if (ms > 0.0 && ms < 250.0) {
                smoothedFrameMs += (ms - smoothedFrameMs) * EMA_ALPHA;
            }
        }
        lastFrameNanos = now;

        int target;
        if (smoothedFrameMs > 26.0) {
            target = 0;
        } else if (smoothedFrameMs > 19.0) {
            target = 1;
        } else if (smoothedFrameMs > 14.0) {
            target = 2;
        } else {
            target = 3;
        }
        // Rise slowly, fall fast: protect the frame rate immediately, restore gently.
        tier = target < tier ? target : (tier + (target > tier ? 1 : 0));
        tier = Math.min(tier, ceiling);
    }

    public static int tier() {
        return tier;
    }

    public static double frameMs() {
        return smoothedFrameMs;
    }
}
