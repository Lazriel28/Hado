package dev.hado.item;

import dev.hado.HadoNetwork;
import dev.hado.fx.FxKind;
import dev.hado.fx.FxStyle;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/** Server-side convenience for "hit something, then make it look incredible". */
public final class HadoFx {
    private HadoFx() {
    }

    public static void play(ServerPlayerEntity source, FxKind kind, FxStyle style, float intensity,
                            Vec3d pos, String title) {
        HadoNetwork.fx(source, kind, style, intensity, pos.x, pos.y, pos.z, title);
    }

    public static void playAt(LivingEntity source, FxKind kind, FxStyle style, float intensity,
                              Vec3d pos, String title) {
        if (source instanceof ServerPlayerEntity player) {
            play(player, kind, style, intensity, pos, title);
        }
    }

    /** Applies player damage and returns whether it landed. */
    public static boolean hurt(ServerWorld world, LivingEntity target, PlayerEntity attacker, float amount) {
        return target.damage(world.getDamageSources().playerAttack(attacker), amount);
    }

    /** Damages the stack in the given hand, if that hand actually holds it. */
    public static void damageHeld(ItemStack stack, PlayerEntity player, Hand hand) {
        stack.damage(1, player, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }
}
