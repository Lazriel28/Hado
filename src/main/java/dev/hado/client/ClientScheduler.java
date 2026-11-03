package dev.hado.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side delayed tasks, used to choreograph the demo sequence.
 *
 * <p>Runs on the client tick rather than wall-clock time so the demo stays in
 * sync with the world even if the frame rate dips.
 */
public final class ClientScheduler {
    private record Task(long dueTick, Runnable action) {
    }

    private static final List<Task> TASKS = new ArrayList<>();
    private static long ticks;

    private ClientScheduler() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    public static void schedule(int delayTicks, Runnable action) {
        TASKS.add(new Task(ticks + delayTicks, action));
    }

    public static void clear() {
        TASKS.clear();
    }

    private static void tick() {
        ticks++;
        if (TASKS.isEmpty()) {
            return;
        }
        for (int i = TASKS.size() - 1; i >= 0; i--) {
            Task task = TASKS.get(i);
            if (task.dueTick() <= ticks) {
                TASKS.remove(i);
                task.action().run();
            }
        }
    }
}
