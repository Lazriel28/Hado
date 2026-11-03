package dev.hado;

import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Sends the single FX packet and a small control-message helper used by the marker
 * remote and marker screen.
 *
 * <p>The FX path is the same one the existing arsenal uses: one small S2C payload
 * per cinematic hit, broadcast to nearby players. The control helper sends the same
 * packet shape but with a title-style message, so the marker GUI and restore cue can
 * be opened from the server side without a separate menu system.
 */
public final class HadoNetwork {    private HadoNetwork() {

    }

    public static void register() {
        // registration is handled by the existing entrypoint path
    }

    public static void fx(ServerPlayerEntity source, FxKind kind, FxStyle style, float intensity,
                          double x, double y, double z, String title) {
        ServerWorld world = source.getServerWorld();
        dev.hado.net.FxPayload payload = dev.hado.net.FxPayload.of(kind, style, intensity, x, y, z,
                world.getRandom().nextInt(), title);
        for (ServerPlayerEntity viewer : world.getPlayers()) {
            if (viewer.squaredDistanceTo(x, y, z) <= 9216.0) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(viewer, payload);
            }
        }
    }

    public static void playControl(ServerPlayerEntity player, String message) {
        ServerWorld world = player.getServerWorld();
        int seed = world.getRandom().nextInt();
        dev.hado.net.FxPayload payload = dev.hado.net.FxPayload.of(FxKind.TITLE, FxStyle.CURSED, 1.0f,
                player.getX(), player.getY(), player.getZ(), seed, message);
        for (ServerPlayerEntity viewer : world.getPlayers()) {
            if (viewer == player || viewer.squaredDistanceTo(player.getX(), player.getY(), player.getZ()) <= 9216.0) {
                net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(viewer, payload);
            }
        }
    }
}
