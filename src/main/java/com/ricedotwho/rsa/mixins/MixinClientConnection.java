package com.ricedotwho.rsa.mixins;

import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.module.impl.dungeon.Dungeonbreaker;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Loc;
import com.ricedotwho.rsm.utils.ItemUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
