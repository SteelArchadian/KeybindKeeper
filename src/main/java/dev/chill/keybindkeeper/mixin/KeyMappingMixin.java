package dev.chill.keybindkeeper.mixin;

import dev.chill.keybindkeeper.KeybindKeeper;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Records each key's original default the moment it's created, before any mod can change it. */
@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void keybindkeeper$recordDefault(CallbackInfo ci) {
        KeybindKeeper.recordDefault((KeyMapping) (Object) this);
    }
}
