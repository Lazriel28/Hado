package dev.hado.item;

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

/**
 * 星屑のリモコン — Starfall Remote. Tunes how loud the mod is allowed to be.
 *
 * <p>The setting itself is client-side (it is a taste, not a rule); the remote
 * only tells the client which level is next. That means it works on a vanilla
 * server too, as long as the client has the mod.
 */
public class StellarRemoteItem extends Item {
    private static final int LEVELS = 4;

    public StellarRemoteItem() {
        super(new Item.Settings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        if (world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
            Vec3d at = user.getEyePos().add(user.getRotationVector().multiply(1.0));

            // Sneak + right-click opens the client tuner; plain right-click steps the level.
            if (user.isSneaking()) {
                HadoFx.play(player, FxKind.TITLE, FxStyle.SCIFI, 1.0f, at, "PANEL");
                serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 0.7f, 1.2f);
                user.getItemCooldownManager().set(this, 8);
                return TypedActionResult.success(stack);
            }

            int level = (HadoPlayerState.cycle(user, 0, LEVELS) + 1) % LEVELS;
            HadoPlayerState.set(user, 0, level);

            HadoFx.play(player, FxKind.TITLE, FxStyle.SCIFI, 1.0f, at, "FX:" + level);
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.PLAYERS, 0.7f, 1.4f);
            user.getItemCooldownManager().set(this, 8);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.stella_remote.tip").formatted(Formatting.GRAY));
    }
}
