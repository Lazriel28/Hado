package dev.hado.client.fx;

/**
 * Camera trauma model. Weapons add trauma, the view shakes, trauma decays.
 *
 * <p>Shake is applied as a yaw/pitch offset derived from smooth multi-frequency
 * noise rather than random per-frame jitter, so it reads as a punch rather than
 * as a broken camera.
 */
public final class ShakeController {
    private static final double DECAY_PER_SECOND = 2.6;

    private static double trauma;
    private static long lastSampleNanos = System.nanoTime();
    private static double time;

    private ShakeController() {
    }

    /** @param amount 0.1 (tap) .. 1.0 (orbital impact) */
    public static void add(double amount) {
        trauma = Math.min(1.0, trauma + amount);
    }

    public static boolean active() {
        return trauma > 0.002;
    }

    public static void reset() {
        trauma = 0.0;
    }

    private static void decay() {
        long now = System.nanoTime();
        double dt = Math.min(0.25, (now - lastSampleNanos) / 1_000_000_000.0);
        lastSampleNanos = now;
        time += dt;
        trauma = Math.max(0.0, trauma - DECAY_PER_SECOND * dt);
    }

    /** Reused output: the camera hook reads this every frame. */
    private static final float[] OUT = new float[3];

    /** Shake amount in degrees for the current frame (yaw, pitch, roll). */
    public static float[] sample(double scale) {
        decay();
        double t = trauma * trauma; // quadratic falloff feels physical
        double amp = t * 5.0 * scale;
        OUT[0] = (float) (amp * (Math.sin(time * 47.0) * 0.6 + Math.sin(time * 91.0 + 1.7) * 0.4));
        OUT[1] = (float) (amp * (Math.sin(time * 57.0 + 0.9) * 0.6 + Math.sin(time * 113.0 + 2.3) * 0.4));
        OUT[2] = (float) (amp * 0.35 * Math.sin(time * 73.0 + 3.1));
        return OUT;
    }
}
