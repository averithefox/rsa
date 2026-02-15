package com.ricedotwho.rsa.mixins;

import com.ricedotwho.rsa.module.impl.dungeon.TermAura;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Redirect(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInvisible()Z"))
    public boolean onGetInvisibility(Entity instance) {
        return !TermAura.getEntityVisibility(instance);
    }
}
