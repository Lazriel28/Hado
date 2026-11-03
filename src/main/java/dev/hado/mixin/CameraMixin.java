package dev.hado.mixin;

import dev.hado.client.config.HadoConfig;
import dev.hado.client.fx.ScreenGate;
import dev.hado.client.fx.ShakeController;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies camera trauma at the very end of the camera update, so the shake is
 * the last word on the view and never fights vanilla camera logic.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V",
            at = @At("TAIL"))
    private void hado$applyShake(BlockView area, Entity focusedEntity, boolean thirdPerson,
                                 boolean inverseView, float tickDelta, CallbackInfo ci) {
        HadoConfig config = HadoConfig.get();
        if (!config.enabled || !ShakeController.active() || !ScreenGate.allowsFx()) {
            return;
        }
        Camera self = (Camera) (Object) this;
        float[] shake = ShakeController.sample(config.shake);
        float yaw = self.getYaw() + shake[0];
        float pitch = MathHelper.clamp(self.getPitch() + shake[1], -90.0f, 90.0f);
        ((CameraAccessor) self).hado$setRotation(yaw, pitch);
    }
}
