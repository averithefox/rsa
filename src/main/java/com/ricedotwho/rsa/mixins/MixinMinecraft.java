package com.ricedotwho.rsa.mixins;

import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 600) // Low prio for SwapManager
public abstract class MixinMinecraft {
    private boolean bl = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickStart(CallbackInfo ci) {
        SwapManager.onPreTickStart(); // Must be called first, unless you have a good reason don't change the order
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 15), method = "handleKeybinds")
        public void onHandleInputEvent(CallbackInfo ci) {
        if (bl) {
            PacketOrderManager.execute(PacketOrderManager.STATE.ITEM_USE);
            bl = false;
            // Need bl because called in whileLoop
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 16), method = "handleKeybinds")
    public void onPostInput(CallbackInfo ci) {
        bl = true;
    }
}
