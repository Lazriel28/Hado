package dev.hado.marker.items;

import dev.hado.HadoNetwork;
import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/** A simple drawn tether to the struck point, pulling a small return. */
public class HadoRecurveMapItem extends Item {
    private static final int COOLDOWN = 20;

    public HadoRecurveMapItem() {
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
            Vec3d at = user.getEyePos().add(user.getRotationVector().multiply(32.0));
            HitResult hit = user.raycast(32.0, 1.0f, false);
            Vec3d target = at;
            boolean hitSomething = false;
            if (hit.getType() == HitResult.Type.BLOCK) {
                target = ((BlockHitResult) hit).getPos().add(0.5, 0.5, 0.5);
                hitSomething = true;
            } else if (hit.getType() == HitResult.Type.ENTITY) {
                LivingEntity entity = ((EntityHitResult) hit).getEntity();
                if (entity instanceof PlayerEntity) {
                    target = entity.getEyePos();
                    hitSomething = true;
                }
            }
            HadoNetwork.fx(player, FxKind.TETHER, FxStyle.CURSED, 1.2f, user.getEyePos().x,
                    user.getEyePos().y, user.getEyePos().z, "TETHER:" + (hitSomething ? "" : "miss"));
            HadoNetwork.fx(player, FxKind.RETURN, FxStyle.CURSED, 1.0f, target.x, target.y, target.z, "RETURN");
            if (hitSomething) {
                serverWorld.playSound(null, target.x, target.y, target.z,
                        SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.5f, 1.0f);
            }
            user.getItemCooldownManager().set(this, COOLDOWN);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.recurve_map.tip").formatted(Formatting.AQUA));
    }
}
