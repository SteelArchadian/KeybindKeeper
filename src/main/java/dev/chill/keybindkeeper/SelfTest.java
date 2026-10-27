package dev.chill.keybindkeeper;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Dev-only self test (-Dkeybindkeeper.selftest=true). Runs once on the title screen, logs [KKTEST] results, then quits. */
public final class SelfTest {

    private static boolean done;

    private SelfTest() {
    }

    @SubscribeEvent
    public static void onTitle(ScreenEvent.Init.Post event) {
        if (done || !(event.getScreen() instanceof TitleScreen title)) {
            return;
        }
        done = true;
        Minecraft mc = Minecraft.getInstance();
        try {
            var file = mc.gameDirectory.toPath().resolve("options.txt");
            boolean kept = KeybindKeeper.ORPHANS.containsKey("key.fakemod.test");
            mc.options.save();
            boolean stillThere = "key.keyboard.k".equals(KeybindKeeper.readKeyLines(file).get("key.fakemod.test"));
            KeybindKeeper.LOG.info("[KKTEST] orphan_kept_across_save {} (collected={}, inFileAfterSave={})", kept && stillThere ? "PASS" : "FAIL", kept, stillThere);

            KeyMapping jump = mc.options.keyJump;
            jump.setKey(InputConstants.getKey("key.keyboard.x"));
            KeybindKeeper.resetToPack();
            KeybindKeeper.LOG.info("[KKTEST] reset_to_pack {} jump={}", jump.saveString().equals("key.keyboard.j") ? "PASS" : "FAIL", jump.saveString());
            boolean otherVanilla = mc.options.keyInventory.saveString().equals("key.keyboard.e");
            KeybindKeeper.LOG.info("[KKTEST] pack_unlisted_key_is_vanilla {} inventory={}", otherVanilla ? "PASS" : "FAIL", mc.options.keyInventory.saveString());

            KeybindKeeper.resetToVanilla();
            KeybindKeeper.LOG.info("[KKTEST] reset_to_vanilla {} jump={}", jump.saveString().equals("key.keyboard.space") ? "PASS" : "FAIL", jump.saveString());

            KeyBindsScreen controls = new KeyBindsScreen(title, mc.options);
            mc.setScreen(controls);
            boolean hasButton = controls.children().stream().anyMatch(c -> c instanceof Button b && b.getMessage().getString().contains("Reset Options"));
            KeybindKeeper.LOG.info("[KKTEST] controls_has_reset_button {}", hasButton ? "PASS" : "FAIL");
        } catch (Throwable t) {
            KeybindKeeper.LOG.error("[KKTEST] FAIL exception", t);
        }
        KeybindKeeper.LOG.info("[KKTEST] DONE");
        mc.stop();
    }
}
