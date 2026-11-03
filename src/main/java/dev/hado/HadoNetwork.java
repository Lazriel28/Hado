package dev.hado;

import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import dev.hado.net.FxPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Sends a single FX packet to every player who could plausibly see the hit.
 * Bounded by distance so a busy server does not fan out packets globally.
 */
public final class HadoNetwork {
    private static final double RANGE = 96.0;
    private static final double RANGE_SQ = RANGE * RANGE;

    private HadoNetwork() {
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(FxPayload.ID, FxPayload.CODEC);
    }

    public static void fx(ServerPlayerEntity source, FxKind kind, FxStyle style, float intensity,
                          double x, double y, double z, String title) {
        ServerWorld world = source.getServerWorld();
        FxPayload payload = FxPayload.of(kind, style, intensity, x, y, z, world.getRandom().nextInt(), title);
        for (ServerPlayerEntity viewer : world.getPlayers()) {
            if (viewer.squaredDistanceTo(x, y, z) <= RANGE_SQ) {
                ServerPlayNetworking.send(viewer, payload);
            }
        }
    }
}
