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
 * 流星砲 — Stellar Cannon. Marks a point, then calls a beam down on it.
 *
 * <p>The telegraph is the point: eight ticks of a shrill charge tone give the
 * target a chance to move, so the huge hit-stop payoff still feels earned.
 */
public class RyuseiCannonItem extends Item {
    private static final int COOLDOWN = 140;
    private static final int CHARGE_TICKS = 8;
    private static final double RANGE = 72.0;
    private static final double BLAST_RADIUS = 4.5;

    public RyuseiCannonItem() {
        super(new Item.Settings().maxDamage(384));
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

        HitResult hit = user.raycast(RANGE, 1.0f, false);
        Vec3d target = hit.getType() == HitResult.Type.MISS
                ? user.getEyePos().add(user.getRotationVector().multiply(RANGE))
                : ((BlockHitResult) hit).getPos();

        user.getItemCooldownManager().set(this, COOLDOWN);
        HadoFx.damageHeld(player.getStackInHand(hand), player, hand);

        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.9f, 1.9f);
        HadoFx.play(player, FxKind.TITLE, FxStyle.SCIFI, 1.0f, target, "RYUSEI CANNON");

        HadoTasks.schedule(serverWorld.getServer(), CHARGE_TICKS, () -> {
            serverWorld.spawnParticles(ParticleTypes.END_ROD, target.x, target.y + 0.4, target.z,
                    60, 1.2, 2.0, 1.2, 0.28);
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, target.x, target.y + 0.4, target.z,
                    2, 0.6, 0.4, 0.6, 0.0);

            for (LivingEntity victim : serverWorld.getEntitiesByClass(LivingEntity.class,
                    Box.of(target, BLAST_RADIUS * 2, BLAST_RADIUS * 2, BLAST_RADIUS * 2),
                    e -> e != player && e.isAlive())) {
                double falloff = Math.max(0.25, 1.0 - victim.getPos().distanceTo(target) / BLAST_RADIUS);
                HadoFx.hurt(serverWorld, victim, player, (float) (18.0 * falloff));
                victim.takeKnockback(1.1, target.x - victim.getX(), target.z - victim.getZ());
            }

            serverWorld.playSound(null, target.x, target.y, target.z,
                    SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.0f, 0.7f);
            HadoFx.play(player, FxKind.ORBITAL, FxStyle.SCIFI, 1.6f, target, null);
        });

        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.ryusei_cannon.tip").formatted(Formatting.AQUA));
    }
}
