package com.ricedotwho.rsa.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.network.Socks5CommandRequestHandler;
import com.ricedotwho.rsa.network.Socks5InitialRequestHandler;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5ClientEncoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponseDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.InetSocketAddress;

import static com.mojang.realmsclient.client.RealmsError.LOGGER;

@Mixin(value = Connection.class, priority = 600) // Needs lower priority for SwapManager
public abstract class MixinClientConnection {

    @Shadow
    @Nullable BandwidthDebugMonitor bandwidthDebugMonitor;

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, CallbackInfo ci) {
        // Don't use Connection.sendPacket
        if (!SwapManager.onPostSendPacket(packet)) {
            ci.cancel();
        }
    }

//    @Inject(method = "connect", at = @At("HEAD"), cancellable = true)
//    private static void onConnect(InetSocketAddress inetSocketAddress, boolean bl, Connection connection, CallbackInfoReturnable<ChannelFuture> cir) {
//        Class<? extends SocketChannel> class_;
//        EventLoopGroup eventLoopGroup;
//        if (Epoll.isAvailable() && bl) {
//            class_ = EpollSocketChannel.class;
//            eventLoopGroup = Connection.NETWORK_EPOLL_WORKER_GROUP.get();
//        } else {
//            class_ = NioSocketChannel.class;
//            eventLoopGroup = Connection.NETWORK_WORKER_GROUP.get();
//        }
//
//        //InetSocketAddress proxyAddress = new InetSocketAddress("chi.socks.ipvanish.com", 1080);
//        InetSocketAddress proxyAddress = new InetSocketAddress("192.252.215.2", 4145);
//
//        ChannelFuture future = new Bootstrap().group(eventLoopGroup).handler(new ChannelInitializer<Channel>() {
//            @Override
//            protected void initChannel(Channel channel) {
//                ChannelPipeline pipeline = channel.pipeline();
//
//                // Add SOCKS5 negotiation handlers FIRST
//                pipeline.addLast(Socks5ClientEncoder.DEFAULT);
//                pipeline.addLast(new Socks5InitialResponseDecoder());
//                pipeline.addLast(new Socks5InitialRequestHandler());
//                pipeline.addLast(new Socks5CommandResponseDecoder());
//                pipeline.addLast(new Socks5CommandRequestHandler(inetSocketAddress));
//
//                try {
//                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
//                } catch (ChannelException var3) {
//                }
//
//                pipeline.addLast("timeout", new ReadTimeoutHandler(30));
//                Connection.configureSerialization(pipeline, PacketFlow.CLIENTBOUND, false, ((ConnectionAccessor) connection).getBandwidthDebugMonitor());
//                connection.configurePacketHandler(pipeline);
//            }
//        }).channel(class_).connect(proxyAddress.getAddress(), proxyAddress.getPort());
//        future.addListener(f -> {
//            if (f.isSuccess()) LOGGER.info("[SOCKS5] Connected to proxy");
//            else LOGGER.error("[SOCKS5] Failed to connect to proxy", f.cause());
//        });
//        cir.setReturnValue(future);
//    }


}
