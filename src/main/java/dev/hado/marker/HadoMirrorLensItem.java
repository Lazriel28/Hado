package dev.hado.marker;

import dev.hado.HadoNetwork;
import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**  mirror lens  Fixes a marker lens flare at a point with a brief lens flare flash. */
public class HadoMirrorLensItem extends Item {
    private static final int COOLDOWN = 14;

    public HadoMirrorLensItem() {
        super(new Item.Settings().maxCount(1).maxDamage(300));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        if (world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
            Vec3d at = user.getEyePos().add(user.getRotationVector().multiply(16.0));
            HitResult hit = user.raycast(24.0, 1.0f, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                at = ((BlockHitResult) hit).getPos();
            }
            String note = "mirror lens @ " + at.x + ", " + at.y + ", " + at.z;
            HadoMarkerStorage.getOrCreate(player).set(at, note);
            HadoNetwork.fx(player, FxKind.LENS, FxStyle.CURSED, 1.3f, at.x, at.y, at.z, "MIRROR LENS");
            serverWorld.playSound(null, at.x, at.y, at.z,
                    SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.9f, 1.3f);
            user.getItemCooldownManager().set(this, COOLDOWN);
            stack.damage(1, user, net.minecraft.entity.EquipmentSlot.MAINHAND);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.mirror_lens.tip").formatted(Formatting.CYAN));
    }
}
