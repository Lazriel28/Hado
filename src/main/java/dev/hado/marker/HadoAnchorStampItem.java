package dev.hado.marker;

import dev.hado.HadoNetwork;
import dev.hado.HadoTasks;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**  anchor stamp  Imprints a small ground marker with a short stamp hit-stop. */
public class HadoAnchorStampItem extends Item {
    private static final int COOLDOWN = 22;

    public HadoAnchorStampItem() {
        super(new Item.Settings().maxCount(16).maxDamage(600));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        if (world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
            Vec3d at = user.getEyePos().add(user.getRotationVector().multiply(5.0));
            HitResult hit = user.raycast(8.0, 1.0f, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                at = ((BlockHitResult) hit).getPos().add(0.5, 0.0, 0.5);
            }
            String note = "anchor stamp @ " + at.x + ", " + at.y + ", " + at.z;
            HadoMarkerStorage.getOrCreate(player).set(at, note);
            HadoNetwork.fx(player, FxKind.STAMP, FxStyle.CURSED, 1.2f, at.x, at.y, at.z, "ANCHOR STAMP");
            serverWorld.playSound(null, at.x, at.y, at.z,
                    SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 0.9f);
            HadoTasks.schedule(serverWorld.getServer(), 6, () -> {
                serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.CRIMSON_SPORE,
                        at.x, at.y + 0.2, at.z, 12, 0.3, 0.2, 0.3, 0.1);
            });
            user.getItemCooldownManager().set(this, COOLDOWN);
            stack.damage(1, user, net.minecraft.entity.EquipmentSlot.MAINHAND);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.anchor_stamp.tip").formatted(Formatting.DARK_BLUE));
    }
}
