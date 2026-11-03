package dev.hado.client.gui;

import dev.hado.client.config.HadoConfig;
import dev.hado.client.fx.DemoSequencer;
import dev.hado.client.fx.FxEngine;
import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import dev.hado.net.FxPayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

/**
 * Live tuner. Every control writes straight into {@link HadoConfig}, so the next
 * effect you trigger already uses the new value — no reload, no restart.
 *
 * <p>The panel deliberately does not pause the game and draws no full-screen
 * background: it exists to be used while looking at the effects it controls.
 */
public class HadoPanelScreen extends Screen {
    private static final int PANEL_W = 316;
    private static final int PANEL_H = 244;
    private static final int ROW = 26;
    private static final int PAD = 12;

    private final List<Runnable> refreshers = new ArrayList<>();

    public HadoPanelScreen() {
        super(Text.literal("HADO"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        refreshers.clear();
        HadoConfig config = HadoConfig.get();

        int left = this.width / 2 - PANEL_W / 2 + PAD;
        int w = PANEL_W - PAD * 2;
        int half = (w - 6) / 2;
        int y = top() + 32;

        addDrawableChild(new TuneSlider(left, y, w, 20, "Intensity", 0.0, 3.0, config.intensity,
                v -> config.intensity = (int) Math.round(v),
                v -> switch ((int) Math.round(v)) {
                    case 0 -> "subtle";
                    case 1 -> "normal";
                    case 2 -> "loud";
                    default -> "maximum";
                }));

        y += ROW;
        addDrawableChild(new TuneSlider(left, y, w, 20, "Camera shake", 0.0, 2.0, config.shake,
                v -> config.shake = (float) v,
                v -> Math.round(v * 100) + "%"));

        y += ROW;
        addDrawableChild(toggle(left, y, half, 20, () -> Text.literal("Hit-stop: " + onOff(config.hitStop)),
                () -> config.hitStop = !config.hitStop));
        addDrawableChild(toggle(left + half + 6, y, half, 20, () -> Text.literal("Grain: " + onOff(config.grain)),
                () -> config.grain = !config.grain));

        y += ROW;
        addDrawableChild(cycle(left, y, half, 20, () -> Text.literal("Quality cap: " + config.qualityCap),
                () -> config.qualityCap = (config.qualityCap + 1) % 4));
        addDrawableChild(cycle(left + half + 6, y, half, 20, () -> Text.literal("Style: " + styleName()),
                () -> config.styleOverride = config.styleOverride >= FxStyle.values().length - 1
                        ? -1 : config.styleOverride + 1));

        y += ROW;
        addDrawableChild(toggle(left, y, half, 20, () -> Text.literal("Effects: " + onOff(config.enabled)),
                () -> config.enabled = !config.enabled));
        addDrawableChild(cycle(left + half + 6, y, half, 20, () -> Text.literal("Reload hado.json"),
                () -> {
                    HadoConfig.load();
                    refreshers.forEach(Runnable::run);
                }));

        y += ROW + 4;
        int third = (w - 12) / 3;
        addDrawableChild(ButtonWidget.builder(Text.literal("Test hit"), b -> testHit())
                .dimensions(left, y, third, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Run demo"), b -> DemoSequencer.start())
                .dimensions(left + third + 6, y, third, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("All effects"), b -> allAtOnce())
                .dimensions(left + (third + 6) * 2, y, third, 20).build());

        y += ROW;
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset to defaults"), b -> {
            config.intensity = 2;
            config.shake = 1.0f;
            config.hitStop = true;
            config.grain = true;
            config.qualityCap = 3;
            config.styleOverride = -1;
            config.enabled = true;
            refreshers.forEach(Runnable::run);
        }).dimensions(left, y, half, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(left + half + 6, y, half, 20).build());
    }

    private int top() {
        return (this.height - PANEL_H) / 2;
    }

    private static String onOff(boolean value) {
        return value ? "on" : "off";
    }

    private String styleName() {
        int override = HadoConfig.get().styleOverride;
        return override < 0 ? "per weapon" : FxStyle.byId(override).name().toLowerCase();
    }

    private ButtonWidget toggle(int x, int y, int w, int h, java.util.function.Supplier<Text> label, Runnable action) {
        return cycle(x, y, w, h, label, action);
    }

    /** A button whose label is recomputed from config after every press. */
    private ButtonWidget cycle(int x, int y, int w, int h, java.util.function.Supplier<Text> label, Runnable action) {
        ButtonWidget button = ButtonWidget.builder(label.get(), b -> {
            action.run();
            refreshers.forEach(Runnable::run);
        }).dimensions(x, y, w, h).build();
        refreshers.add(() -> button.setMessage(label.get()));
        return button;
    }

    private void testHit() {
        PlayerEntity player = this.client == null ? null : this.client.player;
        if (player == null) {
            return;
        }
        Vec3d at = player.getEyePos().add(player.getRotationVector().multiply(4.0));
        FxEngine.play(FxPayload.of(FxKind.IMPACT, currentStyle(), 1.0f,
                at.x, at.y, at.z, (int) System.nanoTime(), "IMPACT"));
    }

    /** Every kind at once — a stress test for the pool and the frame budget. */
    private void allAtOnce() {
        PlayerEntity player = this.client == null ? null : this.client.player;
        if (player == null) {
            return;
        }
        Vec3d at = player.getEyePos().add(player.getRotationVector().multiply(4.0));
        int seed = (int) System.nanoTime();
        int i = 0;
        for (FxKind kind : FxKind.values()) {
            FxEngine.play(FxPayload.of(kind, currentStyle(), 1.4f, at.x, at.y, at.z, seed + i++, null));
        }
    }

    private FxStyle currentStyle() {
        int override = HadoConfig.get().styleOverride;
        return override < 0 ? FxStyle.SHONEN : FxStyle.byId(override);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int left = this.width / 2 - PANEL_W / 2;
        int top = top();

        // No full-screen dimming on purpose: you are tuning what you can see.
        context.fill(left, top, left + PANEL_W, top + PANEL_H, 0xC4101116);
        context.fill(left, top, left + PANEL_W, top + 1, 0xFF4EE8D2);
        context.fill(left, top + PANEL_H - 1, left + PANEL_W, top + PANEL_H, 0xFF4EE8D2);
        context.fill(left, top, left + 1, top + PANEL_H, 0xFF4EE8D2);
        context.fill(left + PANEL_W - 1, top, left + PANEL_W, top + PANEL_H, 0xFF4EE8D2);

        context.drawText(this.textRenderer, "HADO // TUNER", left + PAD, top + 12, 0xFFE8F6FF, false);
        context.drawText(this.textRenderer, "Frame: " + String.format("%.1f", qualityFrameMs()) + " ms",
                left + PANEL_W - PAD - 92, top + 12, 0xFF8892A3, false);

        super.render(context, mouseX, mouseY, delta);
    }

    private double qualityFrameMs() {
        return dev.hado.client.fx.QualityGovernor.frameMs();
    }

    @Override
    public void close() {
        HadoConfig.save();
        super.close();
    }

    /**
     * Compact float slider. Quantisation to whole numbers is the caller's job, so
     * one widget serves both the 0–3 intensity and the 0–2 shake control.
     */
    private static final class TuneSlider extends SliderWidget {
        private final String label;
        private final double min;
        private final double max;
        private final DoubleConsumer applier;
        private final DoubleFunction<String> formatter;

        TuneSlider(int x, int y, int w, int h, String label, double min, double max, double value,
                   DoubleConsumer applier, DoubleFunction<String> formatter) {
            super(x, y, w, h, Text.literal(label), (value - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.applier = applier;
            this.formatter = formatter;
            updateMessage();
        }

        private double current() {
            return min + (max - min) * value;
        }

        @Override
        protected void updateMessage() {
            // SliderWidget's constructor calls this before our fields are assigned.
            if (formatter == null) {
                return;
            }
            setMessage(Text.literal(label + ": " + formatter.apply(current())));
        }

        @Override
        protected void applyValue() {
            applier.accept(current());
        }
    }
}
