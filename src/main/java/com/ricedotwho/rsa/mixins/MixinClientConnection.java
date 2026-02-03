package com.ricedotwho.rsa.mixins;

import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Connection.class, priority = 600) // Needs lower priority for SwapManager
public abstract class MixinClientConnection {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        // Don't use Connection.sendPacket
        if (!SwapManager.onPostSendPacket(packet)) {
            ci.cancel();
        }
    }
}
