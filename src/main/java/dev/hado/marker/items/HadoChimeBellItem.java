package dev.hado.marker.items;

import dev.hado.HadoNetwork;
import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import dev.hado.marker.HadoMarkerStorage;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/** Clears the nearest threat in a small ring and plays a clean chime. */
public class HadoChimeBellItem extends Item {
    private static final int COOLDOWN = 35;

    public HadoChimeBellItem() {
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
            HadoNetwork.fx(player, FxKind.CHIME, FxStyle.CURSED, 1.2f,
                    at.x, at.y, at.z, "CHIME");
            for (var e : serverWorld.getEntitiesByClass(
                    net.minecraft.entity.Entity.class, net.minecraft.AABB.ofSize(at, 12.0, 12.0, 12.0))) {
                if (e instanceof LivingEntity) {
                    ((LivingEntity) e).hurt(serverWorld.getDamageSources().lightning(), 3);
                    if (e instanceof PlayerEntity) {
                        Vec3d eye = e.getEyePos();
                        HadoNetwork.fx(player, FxKind.CLEAR, FxStyle.CURSED, 1.0f,
                                eye.x, eye.y, eye.z, "CLEAR");
                    }
                }
            }
            HadoMarkerStorage.current(player).clear();
            serverWorld.playSound(null, at.x, at.y, at.z,
                    SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 0.7f, 1.4f);
            user.getItemCooldownManager().set(this, COOLDOWN);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.chime_bell.tip").formatted(Formatting.AQUA));
    }
}
