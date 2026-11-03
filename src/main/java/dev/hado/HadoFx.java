package dev.hado;

import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import net.minecraft.server.network.ServerPlayerEntity;

/** Small shared FX helper used by the marker remote and marker screen.
 *
 * <p>Kept intentionally small so there is exactly one public send path for the
 * marker subsystem: fire an FX kind at a point with an optional title cue.
 */
public final class HadoFx {
    private HadoFx() {
    }

    public static void play(ServerPlayerEntity player, FxKind kind, FxStyle style,
                            float intensity, double x, double y, double z, String title) {
        HadoNetwork.fx(player, kind, style, intensity, x, y, z, title);
    }
}
