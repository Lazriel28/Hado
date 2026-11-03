package dev.hado.client.fx;

import dev.hado.client.config.HadoConfig;
import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import dev.hado.net.FxPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The director. Turns an {@link FxPayload} into a pooled {@link ScreenFx}, and
 * decides how much punch it deserves.
 *
 * <p>Allocation discipline: a fixed pool of instances is recycled, the projection
 * scratch objects are static, and a frame with no active effects does no work at
 * all beyond one boolean check.
 */
public final class FxEngine {
    public static final int POOL_SIZE = 16;

    private static final ScreenFx[] POOL = new ScreenFx[POOL_SIZE];
    private static final Quaternionf INVERSE = new Quaternionf();
    private static final Vector3f SCRATCH = new Vector3f();

    private static int activeCount;

    static {
        for (int i = 0; i < POOL_SIZE; i++) {
            POOL[i] = ScreenFx.obtain();
        }
    }

    private FxEngine() {
    }

    /** Timing and punch for each kind. Colour and layers live in the overlay. */
    private record Profile(int durationMs, float shake, int hitStopMs) {
    }

    private static Profile profileOf(FxKind kind) {
        return switch (kind) {
            case IMPACT -> new Profile(430, 0.30f, 70);
            case SLASH -> new Profile(600, 0.28f, 95);
            case ORBITAL -> new Profile(1250, 0.95f, 165);
            case IMPLOSION -> new Profile(900, 0.70f, 130);
            case BARRAGE -> new Profile(330, 0.12f, 0);
            case PARRY -> new Profile(560, 0.22f, 80);
            case DRILL -> new Profile(380, 0.50f, 60);
            case TITLE -> new Profile(950, 0.0f, 0);
        };
    }

    public static void play(FxPayload payload) {
        HadoConfig config = HadoConfig.get();

        // The two gadgets are pure configuration messages, routed before any drawing.
        if (payload.kind() == FxKind.TITLE && payload.title() != null) {
            handleControlMessage(payload.title());
        }
        if (!config.enabled) {
            return;
        }

        Profile profile = profileOf(payload.kind());
        float intensity = payload.intensity() * config.intensityScale();
        if (intensity < 0.05f) {
            return;
        }

        ScreenFx fx = obtain();
        if (fx == null) {
            return;
        }
        fx.reset();
        fx.kind = payload.kind();
        fx.style = config.styleOverride >= 0 ? FxStyle.byId(config.styleOverride) : payload.style();
        fx.intensity = intensity;
        fx.seed = payload.seed();
        fx.startNanos = System.nanoTime();
        fx.durationMs = (long) (profile.durationMs() / Math.max(0.6f, intensity * 0.85f + 0.15f));
        fx.title = payload.title() != null && payload.title().startsWith("STYLE:") ? null : payload.title();

        projectToScreen(payload.x(), payload.y(), payload.z(), fx);

        if (config.hitStop && profile.hitStopMs() > 0 && config.intensity > 0) {
            HitStop.trigger((int) (profile.hitStopMs() * Math.min(1.2f, intensity)));
        }
        if (config.shake > 0.0f) {
            ShakeController.add(profile.shake() * config.shake * Math.min(1.3f, intensity));
        }
    }

    private static void handleControlMessage(String title) {
        HadoConfig config = HadoConfig.get();
        if (title.equals("PANEL")) {
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> client.setScreen(new dev.hado.client.gui.HadoPanelScreen()));
        } else if (title.startsWith("FX:")) {
            try {
                config.intensity = Math.max(0, Math.min(3, Integer.parseInt(title.substring(3))));
                config.save();
            } catch (NumberFormatException ignored) {
                // Malformed message from an unknown sender: ignore rather than crash.
            }
        } else if (title.startsWith("STYLE:")) {
            String name = title.substring(6);
            for (FxStyle style : FxStyle.values()) {
                if (style.name().equals(name)) {
                    config.styleOverride = style.ordinal();
                    config.save();
                }
            }
        }
    }

    private static ScreenFx obtain() {
        if (activeCount >= POOL_SIZE) {
            // Saturated: recycle the oldest slot rather than growing the pool.
            ScreenFx oldest = POOL[0];
            System.arraycopy(POOL, 1, POOL, 0, POOL_SIZE - 1);
            POOL[POOL_SIZE - 1] = oldest;
            activeCount = POOL_SIZE - 1;
        }
        return POOL[activeCount++];
    }

    public static int activeCount() {
        return activeCount;
    }

    public static ScreenFx at(int index) {
        return POOL[index];
    }

    /** Drops expired effects. Called once per frame before rendering. */
    public static void prune() {
        long now = System.nanoTime();
        for (int i = activeCount - 1; i >= 0; i--) {
            if (POOL[i].expired(now)) {
                ScreenFx dead = POOL[i];
                System.arraycopy(POOL, i + 1, POOL, i, activeCount - i - 1);
                POOL[activeCount - 1] = dead;
                activeCount--;
            }
        }
    }

    public static void clear() {
        activeCount = 0;
        HitStop.clear();
        ShakeController.reset();
    }

    /**
     * Projects a world position into scaled GUI pixels. Cheap amortised math:
     * one quaternion conjugate into a scratch vector, no matrix allocation.
     */
    private static void projectToScreen(double x, double y, double z, ScreenFx fx) {
        MinecraftClient client = MinecraftClient.getInstance();
        int w = client.getWindow().getScaledWidth();
        int h = client.getWindow().getScaledHeight();
        fx.screenX = w * 0.5f;
        fx.screenY = h * 0.5f;

        if (client.world == null || client.gameRenderer.getCamera() == null) {
            return;
        }
        Camera camera = client.gameRenderer.getCamera();
        Vec3d cam = camera.getPos();
        SCRATCH.set((float) (x - cam.x), (float) (y - cam.y), (float) (z - cam.z));
        INVERSE.set(camera.getRotation()).conjugate();
        SCRATCH.rotate(INVERSE);

        // Camera space: +x right, +y up, -z forward.
        float depth = -SCRATCH.z;
        if (depth <= 0.12f) {
            return; // behind the player: fall back to centre
        }
        double fov = Math.toRadians(client.options.getFov().getValue());
        float tanHalf = (float) Math.tan(fov * 0.5);
        float aspect = (float) w / Math.max(1, h);
        float ndcX = SCRATCH.x / (depth * tanHalf * aspect);
        float ndcY = SCRATCH.y / (depth * tanHalf);

        fx.screenX = (ndcX * 0.5f + 0.5f) * w;
        fx.screenY = (-ndcY * 0.5f + 0.5f) * h;
    }

    public static boolean hasWork() {
        return activeCount > 0;
    }
}
