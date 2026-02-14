package com.ricedotwho.rsa.mixins;

import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.module.impl.movement.NoRotate;
import com.ricedotwho.rsm.RSM;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {


    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/ClientPacketListener;)V"))
    public void onHandleLogin(CallbackInfo ci) {
        SwapManager.onHandleLogin();
    }

    @Shadow
    public abstract Connection getConnection();

    @Inject(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;setValuesFromPositionPacket(Lnet/minecraft/world/entity/PositionMoveRotation;Ljava/util/Set;Lnet/minecraft/world/entity/Entity;Z)Z", shift = At.Shift.BEFORE), cancellable = true)
    private void onHandlePlayerMove(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        NoRotate noRotate = RSM.getModule(NoRotate.class);
        if (noRotate == null) return;
        noRotate.onHandleMovePlayer(packet, getConnection(), ci);
    }

}
