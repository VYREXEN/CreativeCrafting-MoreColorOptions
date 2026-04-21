package dev.vyrexen.creativecrafting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SlotColorConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("creativecrafting-colors.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static SlotColorConfig instance = new SlotColorConfig();

    public enum Preset {
        DEFAULT,
        DARK,
        CUSTOM
    }

    public Preset preset = Preset.DEFAULT;
    public float customBrightness = 1.0f;

    public static final int DEFAULT_SHADOW  = 0xFF373737;
    public static final int DEFAULT_HILIGHT = 0xFFFFFFFF;
    public static final int DEFAULT_FILL    = 0xFF8B8B8B;

    public static SlotColorConfig getInstance() {
        return instance;
    }

    public int[] getColors() {
        switch (preset) {
            case DARK: {
                return new int[]{
                    blendBlack(DEFAULT_SHADOW,  0.6f),
                    blendBlack(DEFAULT_HILIGHT, 0.6f),
                    blendBlack(DEFAULT_FILL,    0.6f)
                };
            }
            case CUSTOM: {
                float b = Math.max(0f, Math.min(1f, customBrightness));
                return new int[]{
                    scaleBrightness(DEFAULT_SHADOW,  b),
                    scaleBrightness(DEFAULT_HILIGHT, b),
                    scaleBrightness(DEFAULT_FILL,    b)
                };
            }
            default: {
                return new int[]{ DEFAULT_SHADOW, DEFAULT_HILIGHT, DEFAULT_FILL };
            }
        }
    }

    public static int blendBlack(int color, float alpha) {
        int a = (color >> 24) & 0xFF;
        int r = (int)(((color >> 16) & 0xFF) * (1f - alpha));
        int g = (int)(((color >>  8) & 0xFF) * (1f - alpha));
        int b = (int)(( color        & 0xFF) * (1f - alpha));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int scaleBrightness(int color, float brightness) {
        int a = (color >> 24) & 0xFF;
        int r = (int)(((color >> 16) & 0xFF) * brightness);
        int g = (int)(((color >>  8) & 0xFF) * brightness);
        int b = (int)(( color        & 0xFF) * brightness);
        r = Math.min(255, r); g = Math.min(255, g); b = Math.min(255, b);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                instance = GSON.fromJson(Files.readString(CONFIG_PATH), SlotColorConfig.class);
                if (instance == null) instance = new SlotColorConfig();
                LOGGER.info("[CreativeCrafting] Loaded color config: preset={}", instance.preset);
            } else {
                save();
            }
        } catch (IOException e) {
            LOGGER.error("[CreativeCrafting] Failed to load color config", e);
        }
    }

    public static void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(instance));
            LOGGER.info("[CreativeCrafting] Saved color config: preset={}", instance.preset);
        } catch (IOException e) {
            LOGGER.error("[CreativeCrafting] Failed to save color config", e);
        }
    }
}
