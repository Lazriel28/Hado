package dev.hado.client.fx;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.hado.Hado;
import dev.hado.client.config.HadoConfig;
import dev.hado.fx.FxKind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;

/**
 * Draws the cinematic layers over the world.
 *
 * <p>Cost model: every layer is exactly one stretched quad with a pre-baked
 * texture — no per-pixel shader work, no framebuffer copies, no allocation in the
 * hot path. A full orbital strike is five draw calls. Layers are gated by the
 * {@link QualityGovernor} tier so the frame budget decides how loud the mod is.
 */
public final class FxOverlay {
    private static final Identifier[] PLATES = new Identifier[]{
            Hado.id("textures/fx/impact/frame_0.png"),
            Hado.id("textures/fx/impact/frame_1.png"),
            Hado.id("textures/fx/impact/frame_2.png"),
            Hado.id("textures/fx/impact/frame_3.png"),
            Hado.id("textures/fx/impact/frame_4.png"),
    };
    private static final Identifier SPEEDLINES = Hado.id("textures/fx/speedlines.png");
    private static final Identifier SLASH = Hado.id("textures/fx/slash.png");
    private static final Identifier STAR = Hado.id("textures/fx/star.png");
    private static final Identifier RING = Hado.id("textures/fx/ring.png");
    private static final Identifier HEX = Hado.id("textures/fx/hex.png");
    private static final Identifier GRAIN = Hado.id("textures/fx/grain.png");
    private static final Identifier VIGNETTE = Hado.id("textures/fx/vignette.png");

    /** Reused so rotation never allocates a quaternion per layer. */
    private static final Quaternionf ROT = new Quaternionf();

    private FxOverlay() {
    }

    public static void render(DrawContext ctx, int tickDeltaUnused) {
        HadoConfig config = HadoConfig.get();
        QualityGovernor.setCeiling(config.qualityCap);
        QualityGovernor.sample();
        FxEngine.prune();

        if (!config.enabled || !FxEngine.hasWork()) {
            return;
        }

        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();
        long now = System.nanoTime();
        int quality = QualityGovernor.tier();
        int platesLeft = quality >= 2 ? 2 : 1;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (int i = 0; i < FxEngine.activeCount(); i++) {
            ScreenFx fx = FxEngine.at(i);
            float p = fx.progress(now);
            float env = envelope(p);

            switch (fx.kind) {
                case IMPACT, SLASH, ORBITAL, IMPLOSION, PARRY, DRILL -> {
                    if (platesLeft > 0 && p < 0.30f) {
                        platesLeft--;
                        drawPlates(ctx, fx, p, w, h, quality);
                    }
                }
                default -> {
                }
            }

            if (quality >= 2 && fx.kind.wantsSpeedLines() && p < 0.75f) {
                drawSpeedLines(ctx, fx, p, env, w, h);
            }
            if (quality >= 1 && (fx.kind == FxKind.SLASH || fx.kind == FxKind.DRILL)) {
                drawSlash(ctx, fx, p, env, w, h);
            }
            if (quality >= 1 && (fx.kind == FxKind.PARRY || fx.kind == FxKind.IMPLOSION)) {
                drawHex(ctx, fx, p, env, w, h);
            }
            if (quality >= 1 && isRinger(fx.kind)) {
                drawRing(ctx, fx, p, env, w, h, 1.9f, 0.55f);
            }

            if (quality >= 2) {
                drawFlare(ctx, fx, p, env, w, h);
            }
            if (quality >= 1) {
                drawVignette(ctx, fx, p, env, w, h);
            }
            if (quality >= 3 && config.grain && p < 0.6f) {
                drawGrain(ctx, fx, p, env, w, h);
            }

            drawFlash(ctx, fx, p, w, h);

            if (fx.title != null) {
                drawTitle(ctx, fx, p, w, h);
            }
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        // Leave the HUD pass in the state the rest of the GUI expects.
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    /** Fast rise, slow decay — the shape of an anime hit. */
    private static float envelope(float p) {
        if (p < 0.08f) {
            return MathHelper.clamp(p / 0.08f, 0.0f, 1.0f);
        }
        float t = (p - 0.08f) / 0.92f;
        return MathHelper.clamp(1.0f - t * t, 0.0f, 1.0f);
    }

    private static void tint(int argb, float alpha) {
        float a = ((argb >> 24) & 0xFF) / 255.0f * alpha;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        RenderSystem.setShaderColor(r, g, b, MathHelper.clamp(a, 0.0f, 1.0f));
    }

    private static void stretched(DrawContext ctx, Identifier tex, int w, int h) {
        ctx.drawTexture(tex, 0, 0, 0.0f, 0.0f, w, h, w, h);
    }

    // --- layers ---------------------------------------------------------------

    private static void drawPlates(DrawContext ctx, ScreenFx fx, float p, int w, int h, int quality) {
        int frame = Math.min(PLATES.length - 1, (int) (p / 0.30f * PLATES.length));
        // Alternate heavy/light frames: that strobing is the whole aesthetic.
        float alpha = (frame % 2 == 0 ? 0.92f : 0.58f) * envelope(p * 3.3f);
        alpha *= Math.min(1.0f, fx.intensity);
        if (quality == 0) {
            alpha *= 0.6f;
        }
        tint(fx.style.ink, alpha);
        stretched(ctx, PLATES[frame], w, h);

        // Cursed and orbital plates get a coloured key light behind the ink.
        if (quality >= 2) {
            tint(fx.style.accent, alpha * 0.22f);
            stretched(ctx, PLATES[(frame + 2) % PLATES.length], w, h);
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static void drawSpeedLines(DrawContext ctx, ScreenFx fx, float p, float env, int w, int h) {
        float alpha = env * 0.55f * Math.min(1.2f, fx.intensity);
        float angle = (fx.seed % 628) / 100.0f + p * 0.6f;
        tint(fx.style.hot, alpha);
        ctx.getMatrices().push();
        ctx.getMatrices().translate(w * 0.5, h * 0.5, 0.0);
        ROT.rotationZ(angle);
        ctx.getMatrices().multiply(ROT);
        float scale = 1.55f - p * 0.35f;
        int dw = (int) (w * scale);
        int dh = (int) (h * scale);
        ctx.drawTexture(SPEEDLINES, -dw / 2, -dh / 2, 0.0f, 0.0f, dw, dh, dw, dh);
        ctx.getMatrices().pop();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static void drawSlash(DrawContext ctx, ScreenFx fx, float p, float env, int w, int h) {
        float alpha = env * 0.85f;
        float angle = (fx.seed % 360) / 57.2958f;
        tint(fx.style.hot, alpha);
        ctx.getMatrices().push();
        ctx.getMatrices().translate(fx.screenX, fx.screenY, 0.0);
        ROT.rotationZ(angle + (p - 0.5f) * 0.5f);
        ctx.getMatrices().multiply(ROT);
        int dw = (int) (w * 1.25f);
        int dh = (int) (h * 0.30f + p * h * 0.25f);
        ctx.drawTexture(SLASH, -dw / 2, -dh / 2, 0.0f, 0.0f, dw, dh, dw, dh);
        ctx.getMatrices().pop();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static void drawFlare(DrawContext ctx, ScreenFx fx, float p, float env, int w, int h) {
        float alpha = env * 0.85f;
        tint(fx.style.accent, alpha);
        int size = (int) (h * (0.35f + p * 0.55f) * Math.min(1.4f, fx.intensity));
        ctx.drawTexture(STAR, (int) fx.screenX - size / 2, (int) fx.screenY - size / 2,
                0.0f, 0.0f, size, size, size, size);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static void drawRing(DrawContext ctx, ScreenFx fx, float p, float env, int w, int h,
                                 float growth, float alphaScale) {
        float alpha = env * alphaScale;
        tint(fx.style.accent, alpha);
        float grow = 0.4f + p * growth;
        int size = (int) (h * grow * 2.0f);
        ctx.drawTexture(RING, (int) fx.screenX - size / 2, (int) fx.screenY - size / 2,
                0.0f, 0.0f, size, size, size, size);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static void drawHex(DrawContext ctx, ScreenFx fx, float p, float env, int w, int h) {
        tint(fx.style.accent, env * 0.35f);
        float scale = 0.8f + p * 0.6f;
        int dw = (int) (w * scale);
        int dh = (int) (h * scale);
        ctx.drawTexture(HEX, (w - dw) / 2, (h - dh) / 2, 0.0f, 0.0f, dw, dh, dw, dh);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static void drawVignette(DrawContext ctx, ScreenFx fx, float p, float env, int w, int h) {
        float alpha = env * 0.75f * Math.min(1.0f, fx.intensity);
        RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, alpha);
        stretched(ctx, VIGNETTE, w, h);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static void drawGrain(DrawContext ctx, ScreenFx fx, float p, float env, int w, int h) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, env * 0.10f);
        stretched(ctx, GRAIN, w, h);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static boolean isRinger(FxKind kind) {
        return FxKind.isMarkerKind(kind) || kind == FxKind.ECHO || kind == FxKind.YIN_YANG || kind == FxKind.ORBITAL || kind == FxKind.IMPLOSION;
    }

    private static boolean isRinger(FxKind kind) {
        return FxKind.isMarkerKind(kind) || kind == FxKind.ECHO || kind == FxKind.YIN_YANG || kind == FxKind.ORBITAL || kind == FxKind.IMPLOSION;
    }

    private static boolean isTither(FxKind kind) {
        return FxKind.isMarkerKind(kind);
    }

    private static void drawFlash(DrawContext ctx, ScreenFx fx, float p, int w, int h) {
        float flash = switch (fx.kind) {
            case IMPACT -> 0.50f;
            case SLASH -> 0.34f;
            case ORBITAL -> 0.90f;
            case IMPLOSION -> 0.22f;
            case PARRY -> 0.38f;
            case DRILL -> 0.16f;
            case BARRAGE -> 0.14f;
            case TITLE -> 0.0f;
        };
        if (flash <= 0.0f) {
            return;
        }

        // Two-hit flash: a hard spike, then a softer afterglow.
        float a = p < 0.06f ? 1.0f : (float) Math.pow(1.0f - Math.min(1.0f, p / 0.35f), 2.2);
        float alpha = MathHelper.clamp(a * flash * Math.min(1.35f, fx.intensity), 0.0f, 0.92f);
        if (alpha <= 0.005f) {
            return;
        }
        int argb = ((int) (alpha * 255.0f) << 24) | 0xFFFFFF;
        ctx.fill(0, 0, w, h, argb);
    }

    private static void drawTitle(DrawContext ctx, ScreenFx fx, float p, int w, int h) {
        MinecraftClient client = MinecraftClient.getInstance();
        float in = MathHelper.clamp(p / 0.12f, 0.0f, 1.0f);
        float out = MathHelper.clamp((p - 0.72f) / 0.28f, 0.0f, 1.0f);
        float alpha = in * (1.0f - out);
        if (alpha <= 0.01f) {
            return;
        }
        String text = fx.title;
        var font = client.textRenderer;

        int y = (int) (h * 0.26f);
        ctx.getMatrices().push();
        // Slam-in: overshoot then settle.
        float scale = (2.2f + 0.6f * (1.0f - in)) * (1.0f + out * 0.35f);
        ctx.getMatrices().translate(w * 0.5f, y, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);

        int argb = (((int) (alpha * 255)) << 24) | (fx.style.hot() & 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(font, text, 0, 0, argb);
        ctx.drawCenteredTextWithShadow(font, text, -1, 1, (((int) (alpha * 160)) << 24));
        ctx.getMatrices().pop();
    }
}
