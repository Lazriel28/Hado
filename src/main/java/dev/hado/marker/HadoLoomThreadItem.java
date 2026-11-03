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

/**  loom thread  Tosses a tethered marker line from you to where you look. */
public class HadoLoomThreadItem extends Item {
    private static final int COOLDOWN = 20;

    public HadoLoomThreadItem() {
        super(new Item.Settings().maxCount(16).maxDamage(420));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        if (world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
            Vec3d at = user.getEyePos().add(user.getRotationVector().multiply(12.0));
            HitResult hit = user.raycast(16.0, 1.0f, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                at = ((BlockHitResult) hit).getPos();
            }
            Vec3d from = user.getEyePos();
            String note = "thread @ " + from.x + ", " + from.y + ", " + from.z;
            HadoMarkerStorage.getOrCreate(player).set(at, note);
            HadoNetwork.fx(player, FxKind.TETHER, FxStyle.CURSED, 1.1f, at.x, at.y, at.z, "LOOM THREAD");
            serverWorld.playSound(null, at.x, at.y, at.z,
                    SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.PLAYERS, 0.6f, 0.9f);
            user.getItemCooldownManager().set(this, COOLDOWN);
            stack.damage(1, user, net.minecraft.entity.EquipmentSlot.MAINHAND);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.loom_thread.tip").formatted(Formatting.GRAY));
    }
}
