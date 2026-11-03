package dev.hado.client;

import dev.hado.client.config.HadoConfig;
import dev.hado.client.fx.FxEngine;
import dev.hado.client.fx.FxOverlay;
import dev.hado.net.FxPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class HadoClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HadoConfig.load();

        // The whole visual system hangs off one HUD callback.
        HudRenderCallback.EVENT.register((context, tickCounter) -> FxOverlay.render(context, 0));

        ClientScheduler.register();
        HadoClientCommands.register();

        // Packets arrive off the render thread; bounce them onto it so the FX pool
        // is only ever touched from one thread.
        ClientPlayNetworking.registerGlobalReceiver(FxPayload.ID, (payload, context) ->
                context.client().execute(() -> FxEngine.play(payload)));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FxEngine.clear());

        // Reload the config if it was edited while the game sat on the title screen.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) {
                FxEngine.clear();
            }
        });
    }
}
