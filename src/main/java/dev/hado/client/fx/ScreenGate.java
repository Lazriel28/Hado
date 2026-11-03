package dev.hado.client.fx;

import dev.hado.client.gui.HadoPanelScreen;
import net.minecraft.client.MinecraftClient;

/**
 * Decides whether cinematic effects may play right now.
 *
 * <p>Normally any open screen suppresses shake and hit-stop — a menu should never
 * jolt the camera. The tuning panel is the exception: it exists to show you what
 * those effects look like, so it opts itself back in.
 */
public final class ScreenGate {
    private ScreenGate() {
    }

    public static boolean allowsFx() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.currentScreen == null || client.currentScreen instanceof HadoPanelScreen;
    }
}
