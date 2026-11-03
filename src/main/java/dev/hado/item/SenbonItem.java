package dev.hado.item;

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
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * 千本 — Senbon Storm. Five needles fired down one line, spread just enough to
 * catch a runner. No hit-stop here on purpose: a barrage should feel fast.
 */
public class SenbonItem extends Item {
    private static final int NEEDLES = 5;
    private static final double RANGE = 26.0;
    private static final float DAMAGE = 3.0f;
    private static final int COOLDOWN = 18;

    public SenbonItem() {
        super(new Item.Settings().maxCount(1).maxDamage(420));
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

        Vec3d eye = user.getEyePos();
        Vec3d forward = user.getRotationVector();
        Vec3d right = forward.crossProduct(new Vec3d(0, 1, 0)).normalize();
        Vec3d up = right.crossProduct(forward).normalize();

        Vec3d firstHit = null;
        for (int i = 0; i < NEEDLES; i++) {
            double spreadX = (i - (NEEDLES - 1) * 0.5) * 0.045;
            double spreadY = (i % 2 == 0 ? 1 : -1) * 0.02;
            Vec3d dir = forward.add(right.multiply(spreadX)).add(up.multiply(spreadY)).normalize();
            Vec3d end = eye.add(dir.multiply(RANGE));

            HitResult hit = serverWorld.raycast(new net.minecraft.world.RaycastContext(
                    eye, end, net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                    net.minecraft.world.RaycastContext.FluidHandling.NONE, user));
            if (hit.getType() != HitResult.Type.MISS) {
                end = hit.getPos();
            }

            List<LivingEntity> struck = serverWorld.getEntitiesByClass(LivingEntity.class,
                    new Box(eye, end).expand(0.6), e -> e != user && e.isAlive());
            for (LivingEntity victim : struck) {
                if (victim.getBoundingBox().expand(0.4).raycast(eye, end).isPresent()) {
                    HadoFx.hurt(serverWorld, victim, user, DAMAGE);
                    if (firstHit == null) {
                        firstHit = victim.getPos().add(0, victim.getHeight() * 0.6, 0);
                    }
                }
            }
            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, end.x, end.y, end.z, 3, 0.1, 0.1, 0.1, 0.05);
        }

        Vec3d origin = firstHit != null ? firstHit : eye.add(forward.multiply(6));
        HadoFx.play(player, FxKind.BARRAGE, FxStyle.SHONEN, firstHit != null ? 1.1f : 0.7f, origin, null);
        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 0.7f, 2.0f);
        user.getItemCooldownManager().set(this, COOLDOWN);
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.senbon.tip").formatted(Formatting.GRAY));
    }
}
