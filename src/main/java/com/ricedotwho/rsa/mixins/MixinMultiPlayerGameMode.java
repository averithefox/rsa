package com.ricedotwho.rsa.mixins;

import com.ricedotwho.rsa.IMixin.IMultiPlayerGameMode;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode implements IMultiPlayerGameMode {

    @Shadow
    protected abstract void ensureHasSentCarriedItem();

    @Shadow
    protected abstract void startPrediction(ClientLevel clientLevel, PredictiveAction predictiveAction);

    @Override
    public void sendPacketSequenced(ClientLevel world, PredictiveAction packetCreator) {
        this.startPrediction(world, packetCreator);
    }

    @Override
    public void syncSlot() {
        this.ensureHasSentCarriedItem();
    }
}
