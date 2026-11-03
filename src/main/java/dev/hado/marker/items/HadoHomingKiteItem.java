package dev.hado.marker.items;

import dev.hado.HadoNetwork;
import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import dev.hado.marker.HadoMarkerStorage;
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

/** Launches a tethered kite toward the last marker; if none is set it taunts the air. */
public class HadoHomingKiteItem extends Item {
    private static final int COOLDOWN = 40;

    public HadoHomingKiteItem() {
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
            HadoMarkerStorage storage = HadoMarkerStorage.get(player);
            Vec3d target = at.add(user.getRotationVector().multiply(10.0));
            String cue = "KITE";
            if (storage != null && storage.isSet()) {
                target = storage.target();
                cue = "KITE:HOME";
            }
            HadoNetwork.playControl(player, "MARKER:" + cue);
            HadoNetwork.fx(player, FxKind.TAIL, FxStyle.CURSED, 1.1f,
                    at.x, at.y, at.z, "TAIL");
            HadoNetwork.fx(player, FxKind.TETHER, FxStyle.CURSED, 1.0f,
                    target.x, target.y, target.z, "TETHER:" + cue);
            if (storage != null && storage.isSet()) {
                Vec3d back = at.add(target.subtract(at).normalize().multiply(0.8));
                HadoNetwork.fx(player, FxKind.RETURN, FxStyle.CURSED, 1.0f,
                        back.x, back.y, back.z, "RETURN");
                serverWorld.playSound(null, target.x, target.y, target.z,
                        SoundEvents.BLOCK_CONDUIT_ACTIVATE, SoundCategory.PLAYERS, 0.6f, 1.3f);
            }
            user.getItemCooldownManager().set(this, COOLDOWN);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.homing_kite.tip").formatted(Formatting.AQUA));
    }
}
