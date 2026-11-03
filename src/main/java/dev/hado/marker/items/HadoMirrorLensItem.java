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

/** Flips the last event note in the current marker record and plays a lens glint. */
public class HadoMirrorLensItem extends Item {
    private static final int COOLDOWN = 18;

    public HadoMirrorLensItem() {
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
            HadoMarkerStorage storage = HadoMarkerStorage.get(player);
            if (storage != null && storage.isSet()) {
                HadoNetwork.playControl(player, "MARKER:mirror:" + storage.eventNote());
                HadoNetwork.fx(player, FxKind.LENS, FxStyle.OMAKASE, 1.2f,
                        player.getEyePos().x, player.getEyePos().y, player.getEyePos().z, "LENS");
                serverWorld.playSound(null, player.getPos().x, player.getPos().y, player.getPos().z,
                        SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 0.5f, 1.1f);
            } else {
                HadoNetwork.fx(player, FxKind.LENS, FxStyle.OMAKASE, 1.0f,
                        player.getEyePos().x, player.getEyePos().y, player.getEyePos().z, "LENS");
            }
            user.getItemCooldownManager().set(this, COOLDOWN);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.mirror_lens.tip").formatted(Formatting.AQUA));
    }
}
