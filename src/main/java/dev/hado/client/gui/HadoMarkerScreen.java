package dev.hado.client.gui;

import dev.hado.client.config.HadoConfig;
import dev.hado.client.fx.FxEngine;
import dev.hado.client.fx.FxKind;
import dev.hado.client.fx.FxStyle;
import dev.hado.marker.HadoMarker;
import dev.hado.marker.HadoMarkerStorage;
import dev.hado.HadoMarkerRemoteItem;
import dev.hado.net.FxPayload;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;


public class HadoMarkerScreen extends Screen {
    private static final int PANEL_W = 320;
    private static final int PANEL_H = 154;
    private static final int PAD = 12;
    private static final int ROW = 26;

    private final int panelLeft;
    private final int panelTop;
    private final int panelW;
    private final int panelH;

    public HadoMarkerScreen() {
        super(Text.literal("HADO // MARKER"));
        this.panelW = PANEL_W;
        this.panelH = PANEL_H;
        this.panelLeft = -PANEL_W / 2;
        this.panelTop = -PANEL_H / 2;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        refreshButtons();
    }

    private void refreshButtons() {
        PlayerEntity player = client == null ? null : client.player;
        HadoMarkerStorage storage = player == null ? null : HadoMarkerStorage.get(player);
        HadoMarker marker = storage == null ? null : storage.current(player);

        int left = panelLeft + PAD;
        int w = panelW - PAD * 2;
        int half = (w - 6) / 2;
        int y = panelTop + 30;

        addDrawableChild(ButtonWidget.builder(Text.literal(statusText(marker)), b -> {  })
                .dimensions(left, y, w, 20).build());

        y += ROW + 4;
        addDrawableChild(ButtonWidget.builder(Text.literal("Set Marker"), b -> setMarker(player))
                .dimensions(left, y, half, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Clear Marker"), b -> clearMarker(player))
                .dimensions(left + half + 6, y, half, 20).build());

        y += ROW;
        addDrawableChild(ButtonWidget.builder(Text.literal("Restore to Marker"), b -> restoreMarker(player))
                .dimensions(left, y, w, 20).build());

        y += ROW + 4;
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
                .dimensions(left, y, w, 20).build());
    }

    private void setMarker(PlayerEntity player) {
        if (player == null) {
            return;
        }
        Vec3d at = player.getPos();
        String note = "marker saved @ " + at.x + ", " + at.y + ", " + at.z;
        HadoMarkerStorage.getOrCreate(player).set(at, note);
        refreshButtons();
    }

    private void clearMarker(PlayerEntity player) {
        if (player == null) {
            return;
        }
        HadoMarkerStorage.getOrCreate(player).clear();
        refreshButtons();
    }

    private void restoreMarker(PlayerEntity player) {
        if (player == null) {
            return;
        }
        HadoMarker marker = HadoMarkerStorage.get(player);
        if (marker == null || !marker.isSet()) {
            return;
        }
        Vec3d at = player.getEyePos().add(player.getRotationVector().multiply(4.0));
        FxEngine.play(FxPayload.of(FxKind.RETURN, currentStyle(), 1.2f, at.x, at.y, at.z,
                (int) System.nanoTime(), "RESTORE:" + marker.eventNote()));
        close();
    }

    private String statusText(HadoMarker marker) {
        if (marker == null || !marker.isSet()) {
            return "no marker set";
        }
        Vec3d t = marker.target();
        return String.format("marker at %.1f, %.1f, %.1f", t.x, t.y, t.z);
    }

    private FxStyle currentStyle() {
        int override = HadoConfig.get().styleOverride;
        return override < 0 ? FxStyle.CURSED : FxStyle.byId(override);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int left = width / 2 + panelLeft;
        int top = height / 2 + panelTop;

        context.fill(left, top, left + panelW, top + panelH, 0xC4101116);
        context.fill(left, top, left + panelW, top + 1, 0xFF4EE8D2);
        context.fill(left, top + panelH - 1, left + panelW, top + panelH, 0xFF4EE8D2);
        context.fill(left, top, left + 1, top + panelH, 0xFF4EE8D2);
        context.fill(left + panelW - 1, top, left + panelW, top + panelH, 0xFF4EE8D2);

        context.drawText(this.textRenderer, "HADO // MARKER", left + PAD, top + 10, 0xFFE8F6FF, false);
        context.drawText(this.textRenderer, statusText(client == null ? null : HadoMarkerStorage.get(client.player)),
                left + PAD, top + 28, 0xFFE8F6FF, false);
        context.drawText(this.textRenderer, "limited restore: holder cue + teleport, not a world rollback",
                left + PAD, top + panelH - 10, 0xFF8892A3, false);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        super.close();
    }
}
