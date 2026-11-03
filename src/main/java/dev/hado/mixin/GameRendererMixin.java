package dev.hado.mixin;

import dev.hado.client.config.HadoConfig;
import dev.hado.client.fx.HitStop;
import dev.hado.client.fx.ScreenGate;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Hit-stop. While a freeze is armed the world is simply not drawn, but the rest
 * of {@code GameRenderer.render} — including the HUD and therefore the impact
 * overlay — runs as normal.
 *
 * <p>Because only the world draw is skipped, ticks, physics, networking and the
 * player's input all keep running. The freeze is a camera trick, never a stall.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Redirect(method = "render(Lnet/minecraft/client/render/RenderTickCounter;Z)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/render/GameRenderer;renderWorld(Lnet/minecraft/client/render/RenderTickCounter;)V"))
    private void hado$hitStop(GameRenderer self, RenderTickCounter counter) {
        HadoConfig config = HadoConfig.get();
        if (config.enabled && config.hitStop && ScreenGate.allowsFx() && HitStop.isFrozen()) {
            return;
        }
        self.renderWorld(counter);
    }
}
