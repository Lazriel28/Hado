package dev.hado.client.fx;

import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import dev.hado.net.FxPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import static dev.hado.client.ClientScheduler.schedule;

/**
 * Fires every effect kind in a fixed order so you can see — and tune — the whole
 * vocabulary in about five seconds.
 *
 * <p>Each run uses the next palette in rotation, so three runs show all three
 * visual identities without touching the config.
 */
public final class DemoSequencer {
    private static int styleCursor;
    private static int seedCursor = 1;

    private DemoSequencer() {
    }

    public static void start() {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        FxStyle style = FxStyle.values()[styleCursor++ % FxStyle.values().length];
        Vec3d look = player.getRotationVector();
        Vec3d origin = player.getEyePos().add(look.multiply(5.0));
        int seed = seedCursor++ * 7919;

        // Everything is client-side and purely visual, so this needs no packet and
        // works identically in singleplayer and on a server without the mod.
        play(0, FxKind.TITLE, style, 1.0f, origin, seed, "HADO // " + style.name());
        play(9, FxKind.IMPACT, style, 1.05f, origin, seed + 1, "IMPACT");
        play(21, FxKind.SLASH, style, 1.35f, origin, seed + 2, "SLASH");
        play(35, FxKind.BARRAGE, style, 1.0f, origin, seed + 3, "BARRAGE");
        play(47, FxKind.DRILL, style, 1.1f, origin, seed + 4, "DRILL");
        play(59, FxKind.PARRY, style, 1.2f, origin, seed + 5, "PARRY");
        play(73, FxKind.IMPLOSION, style, 1.5f, origin, seed + 6, "IMPLOSION");
        play(89, FxKind.ORBITAL, style, 1.6f, origin, seed + 7, "ORBITAL");
        play(112, FxKind.TITLE, style, 1.0f, origin, seed + 8, "SEQUENCE COMPLETE");
    }

    private static void play(int delay, FxKind kind, FxStyle style, float intensity,
                             Vec3d at, int seed, String title) {
        schedule(delay, () -> FxEngine.play(
                FxPayload.of(kind, style, intensity, at.x, at.y, at.z, seed, title)));
    }
}
