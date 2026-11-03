package dev.hado;

import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import dev.hado.marker.HadoMarkerStorage;
import dev.hado.marker.HadoMarker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipContext;
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

/** Clockwork Reverse Remote.
 *
 * <p>Right-click without sneaking opens the marker screen. Sneak + right-click
 * still opens the existing HADO tuner through the same path the Starfall Remote
 * uses today ("PANEL"). The marker screen lets the holder set a target marker,
 * clear it, and restore to it.
 *
 * <p>Restore is intentionally bounded and cheap: it cues a rewind-style feel, plays
 * a small last-event note back through the client overlay, and teleports the holder
 * to the saved marker target. It is not a world rollback and it does not touch
 * chunk history, block-region state, or other players.
 */
public class HadoMarkerRemoteItem extends Item {
    private static final int COOLDOWN_TICKS = 40;

    public HadoMarkerRemoteItem() {
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

            if (user.isSneaking()) {
                openTuner(serverWorld, player, at);
                return TypedActionResult.success(stack);
            }

            HadoMarkerStorage storage = HadoMarkerStorage.getOrCreate(player);
            HadoMarker marker = storage.get(player);
            if (marker == null || !marker.isSet()) {
                openMarkerScreen(player, at, null);
                return TypedActionResult.success(stack);
            }

            attemptRestore(serverWorld, player, marker, at);
            user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        } else {
            openMarkerScreen(player, at, null);
        }
        }
        return TypedActionResult.success(stack);
    }

    private static void openTuner(ServerWorld serverWorld, ServerPlayerEntity player, Vec3d at) {
        HadoNetwork.fx(player, FxKind.TITLE, FxStyle.SCIFI, 1.0f, at.x, at.y, at.z, "PANEL");
        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 0.7f, 1.2f);
        player.getItemCooldownManager().set(HadoMarkerRemoteItem.this, 8);
    }

    private static void openMarkerScreen(ServerPlayerEntity player, Vec3d at, String initialNote) {
        HadoNetwork.fx(player, FxKind.TITLE, FxStyle.CURSED, 1.0f, at.x, at.y, at.z, "MARKER");
        HadoNetwork.playControl(player, "MARKER:" + (initialNote == null ? "" : initialNote));
        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 0.7f, 1.2f);
    }

    private static void attemptRestore(ServerWorld serverWorld, ServerPlayerEntity player, HadoMarker marker, Vec3d at) {
        Vec3d target = marker.target();
        if (target == null) {
            return;
        }

        HadoNetwork.fx(player, FxKind.RETURN, FxStyle.CURSED, 1.2f, target.x, target.y, target.z, "REWIND");
        HadoNetwork.playControl(player, "RESTORE:" + (marker.eventNote().isEmpty() ? "" : marker.eventNote()));

        double dx = target.x - player.getX();
        double dy = target.y - player.getY();
        double dz = target.z - player.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > 0.5) {
            player.setPosition(target.x, target.y, target.z);
            player.velocityModified = true;
            player.setVelocity(dx * 0.25, 0.4, dz * 0.25);
        }
        serverWorld.playSound(null, target.x, target.y, target.z,
                SoundEvents.ENTITY_ENDERPEARL_TELEPORT, SoundCategory.PLAYERS, 0.8f, 1.2f);
        player.getItemCooldownManager().set(HadoMarkerRemoteItem.this, COOLDOWN_TICKS);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.hado.marker_remote.tip").formatted(Formatting.LIGHT_PURPLE));
    }
}
