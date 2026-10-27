package dev.chill.keybindkeeper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/** Adds "Reset Options..." to the Controls screen (vanilla's and Controlling's, which extends it). */
public final class ControlsButton {

    private ControlsButton() {
    }

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof KeyBindsScreen screen) {
            event.addListener(Button.builder(Component.translatable("keybindkeeper.button.reset"), b -> Minecraft.getInstance().setScreen(new ResetScreen(screen)))
                    .bounds(screen.width - 106, 6, 100, 20)
                    .tooltip(Tooltip.create(Component.translatable("keybindkeeper.button.reset.tooltip")))
                    .build());
        }
    }

    /** Chooser: pack defaults, vanilla defaults, or cancel. */
    public static class ResetScreen extends Screen {

        private final KeyBindsScreen parent;

        ResetScreen(KeyBindsScreen parent) {
            super(Component.translatable("keybindkeeper.reset.title"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int x = this.width / 2 - 100;
            int y = this.height / 2 - 20;
            Button pack = Button.builder(Component.translatable("keybindkeeper.reset.pack"), b -> {
                KeybindKeeper.resetToPack();
                back();
            }).bounds(x, y, 200, 20).build();
            boolean hasPreset = KeybindKeeper.hasPackPreset();
            pack.active = hasPreset;
            if (!hasPreset) {
                pack.setTooltip(Tooltip.create(Component.translatable("keybindkeeper.reset.pack.none")));
            }
            this.addRenderableWidget(pack);
            this.addRenderableWidget(Button.builder(Component.translatable("keybindkeeper.reset.vanilla"), b -> {
                KeybindKeeper.resetToVanilla();
                back();
            }).bounds(x, y + 24, 200, 20).build());
            this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> back()).bounds(x, y + 56, 200, 20).build());
        }

        private void back() {
            // Re-opening re-runs init, which rebuilds the key list with the new bindings.
            Minecraft.getInstance().setScreen(this.parent);
        }

        @Override
        public void onClose() {
            back();
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.render(graphics, mouseX, mouseY, partialTick);
            graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 60, 0xFFFFFF);
            graphics.drawCenteredString(this.font, Component.translatable("keybindkeeper.reset.subtitle"), this.width / 2, this.height / 2 - 44, 0xA0A0A0);
        }
    }
}
