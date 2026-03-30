package com.ricedotwho.rsa.mixins;

import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundInteractPacket.class)
public interface ServerboundInteractPacketAccessor {
    @Accessor("entityId")
    int getEntityID();
}
