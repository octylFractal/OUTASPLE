package net.octyl.mc.outasple.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.octyl.mc.outasple.ServerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class DenySaveMixin {
    @Shadow public abstract ServerLevel overworld();

    @Inject(method = "saveEverything", at = @At("HEAD"), cancellable = true)
    private void denySave(boolean suppressLog, boolean flush, boolean forced, CallbackInfoReturnable<Boolean> cir) {
        if (ServerState.getFor(overworld()).isSkipTickDelay()) {
            cir.setReturnValue(false);
        }
    }
}
