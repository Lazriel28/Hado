package dev.hado;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Tiny server-side scheduler for fuses (bomb delays, follow-up strikes).
 *
 * <p>Deliberately not a ticketed entity: a pending task costs one list slot and
 * one comparison per tick, instead of a loaded chunk and an entity ticking in it.
 */
public final class HadoTasks {
    private record Task(long dueTick, Runnable action) {
    }

    private static final List<Task> TASKS = new ArrayList<>();

    private HadoTasks() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(HadoTasks::tick);
    }

    public static void schedule(MinecraftServer server, int delayTicks, Runnable action) {
        TASKS.add(new Task(server.getTicks() + delayTicks, action));
    }

    private static void tick(MinecraftServer server) {
        if (TASKS.isEmpty()) {
            return;
        }
        long now = server.getTicks();
        // Swap-remove keeps this O(active tasks) with no iterator allocation.
        for (int i = TASKS.size() - 1; i >= 0; i--) {
            Task task = TASKS.get(i);
            if (task.dueTick() <= now) {
                TASKS.remove(i);
                try {
                    task.action().run();
                } catch (RuntimeException e) {
                    Hado.LOG.error("[HADO] Delayed effect failed", e);
                }
            }
        }
    }
}
