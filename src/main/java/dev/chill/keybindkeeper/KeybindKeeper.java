package dev.chill.keybindkeeper;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Keeps keybinds from getting lost, and offers two kinds of reset.
 * <ul>
 *   <li>Vanilla drops a mod's keybind lines from options.txt whenever that mod isn't loaded for a session. Those lines are kept
 *       and written back, so the binds are intact when the mod returns.</li>
 *   <li>"Reset to Pack Defaults" uses configureddefaults/options.txt (the pack's first-install preset).
 *       "Reset to Vanilla" uses each key's original default, recorded when the key is created, before any mod alters it.</li>
 * </ul>
 */
@Mod(value = KeybindKeeper.MOD_ID, dist = Dist.CLIENT)
public class KeybindKeeper {

    public static final String MOD_ID = "keybindkeeper";
    public static final Logger LOG = LogUtils.getLogger();

    /** key_ lines from options.txt for keys that no loaded mod registers right now. name -> saved value. */
    public static final Map<String, String> ORPHANS = new LinkedHashMap<>();
    /** Original default of every key, captured at construction. */
    public static final Map<String, Default> ORIGINAL_DEFAULTS = new HashMap<>();

    public record Default(InputConstants.Key key, KeyModifier modifier) {
    }

    public KeybindKeeper() {
        NeoForge.EVENT_BUS.register(ControlsButton.class);
        if (Boolean.getBoolean("keybindkeeper.selftest")) {
            NeoForge.EVENT_BUS.register(SelfTest.class);
        }
    }

    public static void recordDefault(KeyMapping mapping) {
        ORIGINAL_DEFAULTS.putIfAbsent(mapping.getName(), new Default(mapping.getDefaultKey(), mapping.getDefaultKeyModifier()));
    }

    /** After options.txt is read: remember lines for keys nobody has registered. */
    public static void collectOrphans(Path optionsFile, KeyMapping[] mappings) {
        Map<String, String> lines = readKeyLines(optionsFile);
        Set<String> known = registeredNames(mappings);
        lines.forEach((name, value) -> {
            if (!known.contains(name)) {
                ORPHANS.put(name, value);
            }
        });
        if (!ORPHANS.isEmpty()) {
            LOG.info("Keeping {} keybinds for mods that aren't loaded right now", ORPHANS.size());
        }
    }

    /** After options.txt is written: append the kept lines that are still unregistered. */
    public static void writeOrphans(Path optionsFile, KeyMapping[] mappings) {
        if (ORPHANS.isEmpty()) {
            return;
        }
        Set<String> known = registeredNames(mappings);
        Set<String> written = readKeyLines(optionsFile).keySet();
        List<String> extra = ORPHANS.entrySet().stream()
                .filter(e -> !known.contains(e.getKey()) && !written.contains(e.getKey()))
                .map(e -> "key_" + e.getKey() + ":" + e.getValue()).collect(Collectors.toList());
        if (extra.isEmpty()) {
            return;
        }
        try {
            Files.write(optionsFile, extra, StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOG.warn("Could not keep keybinds for unloaded mods", e);
        }
    }

    /** name -> value, for every key_ line. The name is everything up to the first ':' after "key_". */
    public static Map<String, String> readKeyLines(Path file) {
        Map<String, String> out = new LinkedHashMap<>();
        if (!Files.exists(file)) {
            return out;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.startsWith("key_")) {
                    continue;
                }
                int colon = line.indexOf(':', 4);
                if (colon > 4) {
                    out.put(line.substring(4, colon), line.substring(colon + 1));
                }
            }
        } catch (IOException e) {
            LOG.warn("Could not read {}", file, e);
        }
        return out;
    }

    private static Set<String> registeredNames(KeyMapping[] mappings) {
        return java.util.Arrays.stream(mappings).map(KeyMapping::getName).collect(Collectors.toSet());
    }

    // ------------------------------------------------------------------ resets

    public static Path packPresetFile() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("configureddefaults").resolve("options.txt");
    }

    public static boolean hasPackPreset() {
        return !readKeyLines(packPresetFile()).isEmpty();
    }

    /** Pack preset where it lists a key, vanilla default everywhere else. */
    public static int resetToPack() {
        Map<String, String> preset = readKeyLines(packPresetFile());
        int changed = 0;
        for (KeyMapping mapping : Minecraft.getInstance().options.keyMappings) {
            String value = preset.get(mapping.getName());
            if (value != null) {
                String[] parts = value.split(":");
                KeyModifier modifier = parts.length > 1 ? KeyModifier.valueFromString(parts[1]) : KeyModifier.NONE;
                mapping.setKeyModifierAndCode(modifier, InputConstants.getKey(parts[0]));
            } else {
                setVanilla(mapping);
            }
            changed++;
        }
        finish();
        return changed;
    }

    public static int resetToVanilla() {
        int changed = 0;
        for (KeyMapping mapping : Minecraft.getInstance().options.keyMappings) {
            setVanilla(mapping);
            changed++;
        }
        finish();
        return changed;
    }

    private static void setVanilla(KeyMapping mapping) {
        Default original = ORIGINAL_DEFAULTS.get(mapping.getName());
        if (original != null) {
            mapping.setKeyModifierAndCode(original.modifier(), original.key());
        } else {
            mapping.setToDefault();
        }
    }

    private static void finish() {
        KeyMapping.resetMapping();
        Minecraft.getInstance().options.save();
    }
}
