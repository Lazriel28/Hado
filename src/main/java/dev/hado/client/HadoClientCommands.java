package dev.hado.client;

import com.mojang.brigadier.CommandDispatcher;
import dev.hado.client.config.HadoConfig;
import dev.hado.client.fx.DemoSequencer;
import dev.hado.client.gui.HadoPanelScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

/**
 * {@code /hado} — registered on the client, not the server.
 *
 * <p>Everything it controls is purely visual, so there is no reason to make it a
 * server command: this way it works in singleplayer, on a modded server, and on
 * a vanilla server, and it needs no permission from anyone.
 *
 * <pre>
 *   /hado            open the tuner
 *   /hado demo       play every effect in sequence
 *   /hado panel      open the tuner
 *   /hado reload     re-read config/hado.json
 * </pre>
 */
public final class HadoClientCommands {
    private HadoClientCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> build(dispatcher));
    }

    private static void build(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("hado")
                .executes(context -> {
                    openPanel();
                    return 1;
                })
                .then(ClientCommandManager.literal("demo").executes(context -> {
                    DemoSequencer.start();
                    return 1;
                }))
                .then(ClientCommandManager.literal("panel").executes(context -> {
                    openPanel();
                    return 1;
                }))
                .then(ClientCommandManager.literal("reload").executes(context -> {
                    HadoConfig.load();
                    return 1;
                })));
    }

    private static void openPanel() {
        MinecraftClient client = MinecraftClient.getInstance();
        // Deferred: a command can run before the render thread reaches a safe point.
        client.execute(() -> client.setScreen(new HadoPanelScreen()));
    }
}
