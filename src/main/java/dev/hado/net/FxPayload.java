package dev.hado.net;

import dev.hado.Hado;
import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * The only packet HADO needs: "play this cinematic here".
 *
 * <p>It is a plain value object — the client turns it into visuals and does no
 * world simulation, so a malicious sender can at worst make a screen flash.
 */
public record FxPayload(FxKind kind, FxStyle style, float intensity,
                        double x, double y, double z, int seed, String title) implements CustomPayload {

    public static final CustomPayload.Id<FxPayload> ID = new CustomPayload.Id<>(Hado.id("fx"));

    public static final PacketCodec<RegistryByteBuf, FxPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeVarInt(value.kind.ordinal());
                buf.writeVarInt(value.style.ordinal());
                buf.writeFloat(Math.clamp(value.intensity, 0.0f, 3.0f));
                buf.writeDouble(value.x);
                buf.writeDouble(value.y);
                buf.writeDouble(value.z);
                buf.writeInt(value.seed);
                buf.writeString(value.title, 64);
            },
            buf -> new FxPayload(
                    FxKind.values()[buf.readVarInt() % FxKind.values().length],
                    FxStyle.byId(buf.readVarInt()),
                    buf.readFloat(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readInt(),
                    buf.readString(64)));

    public static FxPayload of(FxKind kind, FxStyle style, float intensity,
                               double x, double y, double z, int seed, String title) {
        return new FxPayload(kind, style, intensity, x, y, z, seed, title);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
