package dev.hado.marker.items;

import dev.hado.HadoNetwork;
import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/** A quick cut-forward dash with a slashed-back ring and a small launch. */
public class HadoSwiftEdgeItem extends Item {
    private static final int COOLDOWN = 10;

    public HadoSwiftEdgeItem() {
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
            Vec3d target = at.add(user.getRotationVector().multiply(6.0));

            HadoNetwork.fx(player, FxKind.SLASHBACK, FxStyle.SCIFI, 1.2f, at.x, at.y, at.z, "SLASHBACK");
            player.setVelocity(user.getRotationVector().multiply(0.7), 0.25);
            player.velocityModified = true;
            user.getItemCooldownManager().set(this, COOLDOWN);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.swift_edge.tip").formatted(Formatting.AQUA));
    }
}
