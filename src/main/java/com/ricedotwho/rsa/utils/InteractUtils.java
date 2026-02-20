package com.ricedotwho.rsa.utils;

import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.MathUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

@UtilityClass
public class InteractUtils implements Accessor {

    /// Call this from {@link PacketOrderManager#register(PacketOrderManager.STATE, Runnable)} or risk a ban!
    public boolean interactOnEntity(Entity entity) {
        if (mc.player == null) return false;
        Vec3 eyePos = mc.player.position().add(0.0d, mc.player.getEyeHeight(), 0.0d);
        Vec3 location = MathUtils.clamp(entity.getBoundingBox(), eyePos).subtract(entity.getX(), entity.getY(), entity.getZ());
        return interactOnEntity(entity, location);
    }

    /// Call this from {@link PacketOrderManager#register(PacketOrderManager.STATE, Runnable)} or risk a ban!
    public boolean interactOnEntity(Entity entity, Vec3 location) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return false;

        for (InteractionHand interactionHand : InteractionHand.values()) {
            ItemStack itemStack = mc.player.getItemInHand(interactionHand);
            if (!itemStack.isItemEnabled(mc.level.enabledFeatures())) {
                return false;
            }

            InteractionResult interactionResult = mc.gameMode.interactAt(mc.player, entity, new EntityHitResult(entity, location), interactionHand);
            if (!interactionResult.consumesAction()) {
                interactionResult = mc.gameMode.interact(mc.player, entity, interactionHand);
            }

            if (interactionResult instanceof InteractionResult.Success success) {
                if (success.swingSource() == InteractionResult.SwingSource.CLIENT) {
                    mc.player.swing(interactionHand);
                }
                return true;
            }
        }
        return true;
    }
}
