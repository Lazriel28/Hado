package dev.hado.item;

import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * 隕鉄ドリル — Meteor Drill. A pickaxe that punches. Right-click vents the drive
 * core for a short burst of haste and a screen of sparks.
 */
public class MeteorDrillItem extends PickaxeItem {
    private static final int COOLDOWN = 220;

    public MeteorDrillItem() {
        super(ToolMaterials.NETHERITE, new Item.Settings()
                .maxDamage(2600)
                .attributeModifiers(PickaxeItem.createAttributeModifiers(ToolMaterials.NETHERITE, 3.5f, -2.6f)));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean ok = super.postHit(stack, target, attacker);
        if (attacker.getWorld() instanceof ServerWorld world && attacker instanceof ServerPlayerEntity player) {
            Vec3d hit = target.getPos().add(0, target.getHeight() * 0.5, 0);
            HadoFx.play(player, FxKind.DRILL, FxStyle.SCIFI, 1.0f, hit, null);
            world.spawnParticles(net.minecraft.particle.ParticleTypes.ELECTRIC_SPARK,
                    hit.x, hit.y, hit.z, 12, 0.3, 0.3, 0.3, 0.45);
            world.playSound(null, hit.x, hit.y, hit.z,
                    SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.5f, 1.8f);
        }
        return ok;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        if (world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 120, 2, false, false, true));
            Vec3d at = user.getEyePos().add(user.getRotationVector().multiply(1.6));
            HadoFx.play(player, FxKind.TITLE, FxStyle.SCIFI, 1.0f, at, "DRIVE OVERCLOCK");
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 0.8f, 1.6f);
            user.getItemCooldownManager().set(this, COOLDOWN);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.meteor_drill.tip").formatted(Formatting.GOLD));
    }
}
