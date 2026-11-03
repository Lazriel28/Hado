package dev.hado.item;

import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * 絶対結界 — Absolute Barrier. A short perfect-guard window: heavy damage
 * reduction plus a hex-grid impact frame when it snaps up.
 */
public class ZettaiBarrierItem extends Item {
    private static final int COOLDOWN = 190;
    private static final int WINDOW_TICKS = 100;

    public ZettaiBarrierItem() {
        super(new Item.Settings().maxCount(1).maxDamage(260));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        if (!(world instanceof ServerWorld serverWorld) || !(user instanceof ServerPlayerEntity player)) {
            return TypedActionResult.success(stack);
        }

        user.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, WINDOW_TICKS, 2, false, false, true));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, WINDOW_TICKS, 1, false, false, true));
        user.getItemCooldownManager().set(this, COOLDOWN);
        HadoFx.damageHeld(player.getStackInHand(hand), player, hand);

        Vec3d at = user.getEyePos().add(user.getRotationVector().multiply(1.2));
        serverWorld.spawnParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 24, 0.9, 0.9, 0.9, 0.02);
        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 0.8f);
        HadoFx.play(player, FxKind.PARRY, FxStyle.SCIFI, 1.2f, at, "ABSOLUTE");
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.zettai_barrier.tip").formatted(Formatting.AQUA));
    }
}
