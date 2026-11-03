package dev.hado.client.fx;

/**
 * Freeze frames. For a few dozen milliseconds the world stops rendering while
 * the HUD keeps drawing, which is what makes a hit land with weight.
 *
 * <p>The freeze is a render-only flag: ticks, physics and networking keep
 * running, so a hit-stop can never desync a server or stall gameplay.
 */
public final class HitStop {
    private static long frozenUntilNanos;

    private HitStop() {
    }

    public static void trigger(int milliseconds) {
        long until = System.nanoTime() + milliseconds * 1_000_000L;
        // Never shorten an existing freeze; take the longest.
        frozenUntilNanos = Math.max(frozenUntilNanos, until);
    }

    public static boolean isFrozen() {
        return System.nanoTime() < frozenUntilNanos;
    }

    public static void clear() {
        frozenUntilNanos = 0L;
    }
}
