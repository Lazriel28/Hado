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

/**  chime bell  Rings a marker chime at a point with a sustained chime ring. */
public class HadoChimeBellItem extends Item {
    private static final int COOLDOWN = 26;

    public HadoChimeBellItem() {
        super(new Item.Settings().maxCount(16));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        if (world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
            Vec3d at = user.getEyePos().add(user.getRotationVector().multiply(2.0));
            HitResult hit = user.raycast(12.0, 1.0f, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                at = ((BlockHitResult) hit).getPos().add(0.5, 0.5, 0.5);
            }
            String note = "chime @ " + at.x + ", " + at.y + ", " + at.z;
            HadoMarkerStorage.getOrCreate(player).set(at, note);
            HadoNetwork.fx(player, FxKind.CHIME, FxStyle.CURSED, 1.0f, at.x, at.y, at.z, "CHIME BELL");
            serverWorld.playSound(null, at.x, at.y, at.z,
                    SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 1.1f);
            user.getItemCooldownManager().set(this, COOLDOWN);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.chime_bell.tip").formatted(Formatting.LIGHT_PURPLE));
    }
}
