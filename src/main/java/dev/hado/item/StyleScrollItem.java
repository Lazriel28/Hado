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
 * 幻想の巻物 — Gensō Scroll. Cycles the cinematic palette: shonen ink, cursed
 * energy, or orbital. Purely a look; it changes no numbers.
 */
public class StyleScrollItem extends Item {
    public StyleScrollItem() {
        super(new Item.Settings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        if (world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
            int next = (HadoPlayerState.cycle(user, 1, FxStyle.values().length) + 1) % FxStyle.values().length;
            HadoPlayerState.set(user, 1, next);
            FxStyle style = FxStyle.byId(next);

            Vec3d at = user.getEyePos().add(user.getRotationVector().multiply(1.0));
            HadoFx.play(player, FxKind.TITLE, style, 1.0f, at, "STYLE:" + style.name());
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS, 1.0f, 1.1f);
            user.getItemCooldownManager().set(this, 10);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.style_scroll.tip").formatted(Formatting.LIGHT_PURPLE));
    }
}
