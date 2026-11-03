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

/** Writes a short note into the current marker record and reads it back from nearby players. */
public class HadoCounterScribeItem extends Item {
    private static final int COOLDOWN = 15;

    public HadoCounterScribeItem() {
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
            String note = storage == null || !storage.isSet()
                    ? "no marker to annotate"
                    : storage.eventNote();
            HadoNetwork.playControl(player, "MARKER:scribe:" + note);
            if (storage != null && storage.isSet()) {
                HadoNetwork.fx(player, FxKind.REVEAL, FxStyle.CURSED, 1.0f,
                        player.getEyePos().x, player.getEyePos().y, player.getEyePos().z, "REVEAL");
            }
            serverWorld.playSound(null, player.getPos().x, player.getPos().y, player.getPos().z,
                    SoundEvents.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 0.6f, 1.2f);
            user.getItemCooldownManager().set(this, COOLDOWN);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.counter_scribe.tip").formatted(Formatting.AQUA));
    }
}
