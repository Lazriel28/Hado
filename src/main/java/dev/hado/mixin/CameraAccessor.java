package dev.hado.mixin;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** {@code Camera#setRotation} is protected; this exposes it to the shake hook. */
@Mixin(Camera.class)
public interface CameraAccessor {
    @Invoker("setRotation")
    void hado$setRotation(float yaw, float pitch);
}
