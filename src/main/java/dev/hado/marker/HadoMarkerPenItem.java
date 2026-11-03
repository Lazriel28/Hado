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

/**  marker pen  The reverse remote's marker-creator pen: sets a marker target at the looked-at point. */
public class HadoMarkerPenItem extends Item {
    private static final int COOLDOWN = 18;

    public HadoMarkerPenItem() {
        super(new Item.Settings().maxCount(16).maxDamage(240));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        if (world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
            Vec3d at = user.getEyePos().add(user.getRotationVector().multiply(4.0));
            HitResult hit = user.raycast(6.0, 1.0f, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                at = ((BlockHitResult) hit).getPos().add(0.5, 0.0, 0.5);
            }
            String note = "marker pen @ " + at.x + ", " + at.y + ", " + at.z;
            HadoMarkerStorage.getOrCreate(player).set(at, note);
            HadoNetwork.playControl(player, "MARKER:" + note);
            HadoNetwork.fx(player, FxKind.BEACON, FxStyle.CURSED, 1.0f, at.x, at.y, at.z, "MARKER PEN");
            serverWorld.playSound(null, at.x, at.y, at.z,
                    SoundEvents.BLOCK_WRITABLE_BOOK_TURN, SoundCategory.PLAYERS, 0.7f, 1.0f);
            user.getItemCooldownManager().set(this, COOLDOWN);
            stack.damage(1, user, net.minecraft.entity.EquipmentSlot.MAINHAND);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.marker_pen.tip").formatted(Formatting.AQUA));
    }
}
