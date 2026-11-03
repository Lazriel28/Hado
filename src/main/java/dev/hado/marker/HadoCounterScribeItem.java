package dev.hado.marker;

import dev.hado.HadoFx;
import dev.hado.HadoNetwork;
import dev.hado.HadoTasks;
import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import net.minecraft.entity.LivingEntity;
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

/**  counter scribe  A delayed marker mark; nearby targets get a small returned scribe hit. */
public class HadoCounterScribeItem extends Item {
    private static final int COOLDOWN = 34;
    private static final double RADIUS = 3.5;
    private static final float MARK_DAMAGE = 4.0f;

    public HadoCounterScribeItem() {
        super(new Item.Settings().maxCount(1).maxDamage(560));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        if (world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
            Vec3d at = user.getEyePos().add(user.getRotationVector().multiply(8.0));
            HitResult hit = user.raycast(9.0, 1.0f, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                at = ((BlockHitResult) hit).getPos();
            }
            String note = "counter scribe @ " + at.x + ", " + at.y + ", " + at.z;
            HadoMarkerStorage.getOrCreate(player).set(at, note);
            HadoNetwork.fx(player, FxKind.REVEAL, FxStyle.CURSED, 1.1f, at.x, at.y, at.z, "COUNTER SCRIBE");
            serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.PLAYERS, 0.7f, 1.4f);
            HadoTasks.schedule(serverWorld.getServer(), 10, () -> {
                HadoNetwork.fx(player, FxKind.SLASHBACK, FxStyle.CURSED, 1.0f, at.x, at.y, at.z, "SCRIBE RETURN");
                for (LivingEntity victim : serverWorld.getEntitiesByClass(LivingEntity.class,
                        Box.of(at, RADIUS * 2, RADIUS * 2, RADIUS * 2),
                        e -> e != player && e.isAlive())) {
                    HadoFx.hurt(serverWorld, victim, player, MARK_DAMAGE);
                }
            });
            user.getItemCooldownManager().set(this, COOLDOWN);
            stack.damage(2, user, net.minecraft.entity.EquipmentSlot.MAINHAND);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.counter_scribe.tip").formatted(Formatting.DARK_BLUE));
    }
}
