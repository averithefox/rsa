package com.ricedotwho.rsa.mixins;

import com.ricedotwho.rsa.IMixin.IMultiPlayerGameMode;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode implements IMultiPlayerGameMode {

    @Shadow
    protected abstract void ensureHasSentCarriedItem();

    @Shadow
    protected abstract void startPrediction(ClientLevel clientLevel, PredictiveAction predictiveAction);

    @Shadow
    private int carriedIndex;

    @Override
    public void sendPacketSequenced(ClientLevel world, PredictiveAction packetCreator) {
        this.startPrediction(world, packetCreator);
    }

    @Override
    public void syncSlot() {
        this.ensureHasSentCarriedItem();
    }

    @Inject(method = "ensureHasSentCarriedItem", at = @At("HEAD"), cancellable = true)
    public void onSyncSlot(CallbackInfo ci) {
        if (!SwapManager.onEnsureHasSentCarriedItem(this.carriedIndex)) ci.cancel();
    }
}
