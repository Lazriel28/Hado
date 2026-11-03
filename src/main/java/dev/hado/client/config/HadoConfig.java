package dev.hado.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.hado.Hado;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** User-facing taste controls. Everything here is visual only — nothing in this
 *  file can change gameplay outcomes, which is why it lives on the client.
 */
public final class HadoConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("hado.json");

    private static HadoConfig instance = new HadoConfig();

    /** Master switch: false = vanilla, zero overlay work. */
    public boolean enabled = true;
    /** 0 = subtle, 1 = normal, 2 = loud, 3 = maximum. */
    public int intensity = 2;
    /** Freeze frames on heavy hits. */
    public boolean hitStop = true;
    /** Camera trauma multiplier, 0.0 - 2.0. */
    public float shake = 1.0f;
    /** -1 = use each weapon's own palette, otherwise an FxStyle ordinal. */
    public int styleOverride = -1;
    /** 0..3 hard cap on the adaptive quality tier. */
    public int qualityCap = 3;
    /** Film grain pass. */
    public boolean grain = true;

    public static HadoConfig get() {
        return instance;
    }

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                HadoConfig loaded = GSON.fromJson(Files.readString(PATH), HadoConfig.class);
                if (loaded != null) {
                    instance = loaded;
                }
            } else {
                save();
            }
        } catch (Exception e) {
            Hado.LOG.warn("[HADO] Could not read config, using defaults", e);
        }
        instance.clamp();
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(instance));
        } catch (IOException e) {
            Hado.LOG.warn("[HADO] Could not write config", e);
        }
    }

    public void clamp() {
        intensity = Math.max(0, Math.min(3, intensity));
        shake = Math.max(0.0f, Math.min(2.0f, shake));
        qualityCap = Math.max(0, Math.min(3, qualityCap));
        if (styleOverride < -1) {
            styleOverride = -1;
        }
    }

    /** Multiplier applied to every effect's intensity. */
    public float intensityScale() {
        return switch (intensity) {
            case 0 -> 0.45f;
            case 1 -> 0.75f;
            case 2 -> 1.0f;
            default -> 1.3f;
        };
    }
}
