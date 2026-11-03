package dev.hado.marker;

import dev.hado.HadoFx;
import dev.hado.HadoNetwork;
import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
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

/**  swift edge  A returned slash: quick, sharp, with a decisive hit-stop. */
public class HadoSwiftEdgeItem extends Item {
    private static final int COOLDOWN = 24;

    public HadoSwiftEdgeItem() {
        super(new Item.Settings().maxCount(1).maxDamage(420));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        if (world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
            Vec3d at = user.getEyePos().add(user.getRotationVector().multiply(3.0));
            HadoNetwork.fx(player, FxKind.SLASHBACK, FxStyle.CURSED, 1.3f, at.x, at.y, at.z, "SWIFT EDGE");
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.8f, 1.0f);
            user.getItemCooldownManager().set(this, COOLDOWN);
            stack.damage(1, user, net.minecraft.entity.EquipmentSlot.MAINHAND);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean ok = super.postHit(stack, target, attacker);
        if (attacker.getWorld() instanceof ServerWorld world && attacker instanceof ServerPlayerEntity player) {
            Vec3d hit = target.getPos().add(0, target.getHeight() * 0.5, 0);
            HadoFx.play(player, FxKind.SLASHBACK, FxStyle.CURSED, 1.2f, hit, null);
            world.spawnParticles(net.minecraft.particle.ParticleTypes.CRIT, hit.x, hit.y, hit.z, 10, 0.2, 0.2, 0.2, 0.3);
        }
        return ok;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.swift_edge.tip").formatted(Formatting.DARK_BLUE));
    }
}
