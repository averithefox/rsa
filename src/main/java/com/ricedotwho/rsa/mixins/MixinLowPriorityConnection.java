package com.ricedotwho.rsa.mixins;

import com.ricedotwho.rsa.IMixin.IConnection;
import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.module.impl.dungeon.boss.Blink;
import com.ricedotwho.rsa.module.impl.dungeon.boss.VelocityBuffer;
import com.ricedotwho.rsm.component.impl.EventComponent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Connection.class, priority = 400) // RSM is at 499
public abstract class MixinLowPriorityConnection implements IConnection {
    @Shadow
    protected abstract void sendPacket(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean bl);

    // This gets called earlier, before other hooks hopefully and isin't triggered by receivePacket
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V"), cancellable = true)
    private void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketOrderManager.onPreReceivePacket(packet); // Packet may get canceled by velocity buffer

        if (VelocityBuffer.onReceivePacketPre(packet)) {
            ci.cancel();
        }
    }

    // This gets called earlier, before other hooks hopefully and isin't triggered by receivePacket
    @Inject(method = "sendPacket", at = @At(value = "HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean bl, CallbackInfo ci) {
        if (Blink.onSendPacket(packet)) {
            ci.cancel();
        }
    }


    @Override
    public void sendPacketImmediately(Packet<?> packet) {
        this.sendPacket(packet, null, true);
    }
}
