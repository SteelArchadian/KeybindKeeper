package dev.chill.keybindkeeper.mixin;

import dev.chill.keybindkeeper.KeybindKeeper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Options.class)
public abstract class OptionsMixin {

    @Shadow @Final private File optionsFile;
    @Shadow public KeyMapping[] keyMappings;

    @Inject(method = "load()V", at = @At("TAIL"))
    private void keybindkeeper$afterLoad(CallbackInfo ci) {
        KeybindKeeper.collectOrphans(this.optionsFile.toPath(), this.keyMappings);
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void keybindkeeper$afterSave(CallbackInfo ci) {
        KeybindKeeper.writeOrphans(this.optionsFile.toPath(), this.keyMappings);
    }
}
