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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**  drift compass  Cues a soft homing pulse toward the saved marker target. */
public class HadoDriftCompassItem extends Item {
    private static final int COOLDOWN = 18;

    public HadoDriftCompassItem() {
        super(new Item.Settings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        if (world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
            HadoMarker marker = HadoMarkerStorage.get(player);
            Vec3d target = marker != null ? marker.target() : player.getPos();
            String note = marker != null && marker.isSet() ? marker.eventNote() : "drift toward marker";
            HadoNetwork.fx(player, FxKind.GLOW, FxStyle.CURSED, 1.0f, target.x, target.y, target.z, "DRIFT COMPASS");
            HadoNetwork.playControl(player, "RESTORE:" + note);
            serverWorld.playSound(null, target.x, target.y, target.z,
                    SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.7f, 1.0f);
            user.getItemCooldownManager().set(this, COOLDOWN);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.drift_compass.tip").formatted(Formatting.AQUA));
    }
}
