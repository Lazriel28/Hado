package dev.hado.marker.items;

import dev.hado.HadoNetwork;
import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/** A held compass that drifts in the hand toward nearby threats and rings. */
public class HadoDriftCompassItem extends Item {
    private static final int COOLDOWN = 25;

    public HadoDriftCompassItem() {
        super(new Item.Settings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        if (world instanceof net.minecraft.server.world.ServerWorld serverWorld
                && user instanceof ServerPlayerEntity player) {
            Vec3d at = user.getEyePos();
            Vec3d target = at;
            boolean reacted = false;
            var threats = serverWorld.getEntitiesByClass(net.minecraft.entity.Entity.class,
                    net.minecraft.AABB.ofSize(at, 32.0, 32.0, 32.0),
                    entity -> entity instanceof LivingEntity && entity != player);
            if (threats.size() > 0) {
                LivingEntity closest = threats.get(0);
                double best = at.squaredDistanceTo(closest.getEyePos());
                for (int i = 1; i < threats.size(); i++) {
                    double d = at.squaredDistanceTo(threats.get(i).getEyePos());
                    if (d < best) {
                        best = d;
                        closest = threats.get(i);
                    }
                }
                Vec3d eye = closest.getEyePos();
                target = at.add(eye.subtract(at));
                reacted = true;
            }
            HadoNetwork.fx(player, FxKind.GLOW, FxStyle.CURSED, 1.0f,
                    target.x, target.y, target.z, "DRIFT");
            serverWorld.playSound(null, target.x, target.y, target.z,
                    SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.4f, 1.0f);
            user.getItemCooldownManager().set(this, COOLDOWN);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.drift_compass.tip").formatted(Formatting.AQUA));
    }
}
