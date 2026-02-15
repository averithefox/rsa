package com.ricedotwho.rsa.mixins;

import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.module.impl.movement.NoRotate;
import com.ricedotwho.rsm.RSM;
import net.minecraft.client.Camera;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Camera.class)
public abstract class CameraMixin {

//    @Inject(method = "getPosition", at = @At("HEAD"), cancellable = true)
//    private void onGetPosition(CallbackInfoReturnable<Vec3> cir) {
//        NoRotate noRotate = RSM.getModule(NoRotate.class);
//        if (noRotate.isEnabled()) {
//            Vec3 pos = noRotate.getCameraPos();
//            if (pos == null) return;
//            cir.setReturnValue(pos);
//        }
//    }
}
