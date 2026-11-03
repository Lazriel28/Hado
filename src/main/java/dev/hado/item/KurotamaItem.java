package dev.hado.item;

import dev.hado.HadoTasks;
import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * 黒玉 — Kurotama. A cursed core with a two-second fuse. It implodes instead of
 * exploding: everything inside the radius is pulled in, then everything is gone.
 */
public class KurotamaItem extends Item {
    private static final int FUSE_TICKS = 34;
    private static final double THROW_RANGE = 28.0;
    private static final double RADIUS = 4.2;

    public KurotamaItem() {
        super(new Item.Settings().maxCount(16));
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

        // Aim: the block under the crosshair, so the bomb lands where you look.
        HitResult hit = user.raycast(THROW_RANGE, 1.0f, false);
        Vec3d landing = hit.getType() == HitResult.Type.MISS
                ? user.getEyePos().add(user.getRotationVector().multiply(THROW_RANGE))
                : ((BlockHitResult) hit).getPos();

        user.getItemCooldownManager().set(this, 26);
        if (!user.getAbilities().creativeMode) {
            user.getStackInHand(hand).decrement(1);
        }
        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS, 0.8f, 0.6f);

        HadoTasks.schedule(serverWorld.getServer(), FUSE_TICKS, () -> {
            serverWorld.spawnParticles(ParticleTypes.SQUID_INK, landing.x, landing.y + 0.3, landing.z,
                    40, 1.4, 1.0, 1.4, 0.08);
            serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, landing.x, landing.y + 0.3, landing.z,
                    90, 1.6, 1.2, 1.6, 0.35);

            for (LivingEntity victim : serverWorld.getEntitiesByClass(LivingEntity.class,
                    Box.of(landing, RADIUS * 2, RADIUS * 2, RADIUS * 2),
                    e -> e != user && e.isAlive())) {
                double dist = Math.max(0.6, victim.getPos().distanceTo(landing));
                // Pull toward the core, then hit.
                Vec3d pull = landing.subtract(victim.getPos()).normalize().multiply(0.55);
                victim.setVelocity(pull.x, 0.35, pull.z);
                victim.velocityModified = true;
                HadoFx.hurt(serverWorld, victim, user, (float) Math.max(2.0, 16.0 - dist * 1.8));
            }

            serverWorld.playSound(null, landing.x, landing.y, landing.z,
                    SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS, 0.75f, 1.35f);
            HadoFx.play(player, FxKind.IMPLOSION, FxStyle.CURSED, 1.5f, landing, null);
        });
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.kurotama.tip").formatted(Formatting.DARK_PURPLE));
    }
}
