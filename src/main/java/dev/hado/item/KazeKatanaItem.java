package dev.hado.item;

import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * 風牙 — Gale Fang. A katana that trades raw damage for a dash-cut that drags
 * every enemy in front of you through one continuous slash frame.
 */
public class KazeKatanaItem extends SwordItem {
    private static final int DASH_COOLDOWN = 34;
    private static final double DASH_RANGE = 5.0;

    public KazeKatanaItem() {
        super(ToolMaterials.NETHERITE, new Item.Settings()
                .maxDamage(2100)
                .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.NETHERITE, 3, -1.8f)));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean ok = super.postHit(stack, target, attacker);
        if (attacker.getWorld() instanceof ServerWorld world && attacker instanceof ServerPlayerEntity player) {
            Vec3d hit = target.getPos().add(0, target.getHeight() * 0.6, 0);
            HadoFx.play(player, FxKind.IMPACT, FxStyle.SHONEN, 0.85f, hit, null);
            world.spawnParticles(net.minecraft.particle.ParticleTypes.CRIT,
                    hit.x, hit.y, hit.z, 8, 0.25, 0.25, 0.25, 0.35);
        }
        return ok;
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

        Vec3d look = user.getRotationVector();
        user.setVelocity(look.x * 1.45, Math.max(0.14, look.y * 0.4 + 0.12), look.z * 1.45);
        user.velocityModified = true;

        int hits = 0;
        Vec3d eye = user.getEyePos();
        for (LivingEntity target : serverWorld.getEntitiesByClass(LivingEntity.class,
                new Box(eye, eye.add(look.multiply(DASH_RANGE))).expand(2.4),
                e -> e != user && e.isAlive())) {
            Vec3d to = target.getPos().subtract(user.getPos()).normalize();
            if (to.dotProduct(look) < 0.35) {
                continue;
            }
            HadoFx.hurt(serverWorld, target, user, 7.0f);
            target.takeKnockback(0.6, -look.x, -look.z);
            hits++;
        }

        Vec3d origin = eye.add(look.multiply(2.2));
        HadoFx.play(player, FxKind.SLASH, FxStyle.SHONEN, hits > 0 ? 1.35f : 0.9f, origin, "GALE FANG");
        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1.0f, 0.75f);
        user.getItemCooldownManager().set(this, DASH_COOLDOWN);
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.kaze_katana.tip").formatted(Formatting.AQUA));
    }
}
