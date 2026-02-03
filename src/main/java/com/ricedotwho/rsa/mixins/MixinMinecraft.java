package com.ricedotwho.rsa.mixins;

import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 600) // Low prio for SwapManager
public abstract class MixinMinecraft {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        SwapManager.onPreTickStart(); // Must be called first, unless you have a good reason don't change the order

    }
}
