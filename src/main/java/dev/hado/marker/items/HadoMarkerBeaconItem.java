package dev.hado.marker.items;

import dev.hado.HadoNetwork;
import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import dev.hado.marker.HadoMarkerStorage;
import dev.hado.Hado;
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
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/** Prompts the marker by a beacon pulse and returns to it when redirected. */
public class HadoMarkerBeaconItem extends Item {
    private static final int COOLDOWN = 30;

    public HadoMarkerBeaconItem() {
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
            Vec3d at = user.getEyePos().add(user.getRotationVector().multiply(1.6));
            HitResult hit = user.raycast(32.0, 1.0f, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                at = ((BlockHitResult) hit).getPos().add(0.5, 0.0, 0.5);
            }
            String note = "marker @ " + at.x + ", " + at.y + ", " + at.z;
            HadoMarkerStorage.getOrCreate(player).set(at, note);
            HadoNetwork.playControl(player, "MARKER:" + note);
            HadoNetwork.fx(player, FxKind.BEACON, FxStyle.CURSED, 1.2f, at.x, at.y, at.z, "BEACON");
            serverWorld.playSound(null, at.x, at.y, at.z,
                    SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.9f, 1.2f);
            user.getItemCooldownManager().set(this, COOLDOWN);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.marker_beacon.tip").formatted(Formatting.AQUA));
    }
}
