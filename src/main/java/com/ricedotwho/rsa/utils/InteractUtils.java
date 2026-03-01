package com.ricedotwho.rsa.utils;

import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.MathUtils;
import com.ricedotwho.rsm.utils.RotationUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
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

    /// Call this from {@link PacketOrderManager#register(PacketOrderManager.STATE, Runnable)} or risk a ban!
    public boolean interactOnBlock(BlockPos pos, boolean swing) {
        if (mc.player == null || mc.level == null) return false;
        Vec3 eyePos = mc.player.position().add(0.0d, mc.player.getEyeHeight(), 0.0d);
        return interactOnBlock(pos, eyePos, swing);
    }

    /// Call this from {@link PacketOrderManager#register(PacketOrderManager.STATE, Runnable)} or risk a ban!
    public boolean interactOnBlock(BlockPos pos, Vec3 eyePos, boolean swing) {
        if (mc.level == null) return false;
        BlockState blockState = mc.level.getBlockState(pos);
        AABB blockAABB = blockState.getShape(mc.level, pos).bounds();

        Vec3 center = new Vec3((blockAABB.minX + blockAABB.maxX) * 0.5 + pos.getX(), (blockAABB.minY + blockAABB.maxY) * 0.5 + pos.getY(), (blockAABB.minZ + blockAABB.maxZ) * 0.5 + pos.getZ());
        BlockHitResult result = RotationUtils.collisionRayTrace(pos, blockAABB, eyePos, center);
        if (result == null) return false;

        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
            SwapManager.sendBlockC08(result.getLocation(), result.getDirection(), swing, true);
        });
        return true;
    }

    /// Call this from {@link PacketOrderManager#register(PacketOrderManager.STATE, Runnable)} in {@link PacketOrderManager.STATE#ATTACK} or risk a ban!
    public boolean attackEntity(Entity entity) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return false;

        ItemStack itemStack = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!itemStack.isItemEnabled(mc.level.enabledFeatures())) {
            return false;
        }

        mc.gameMode.attack(mc.player, entity);
        mc.player.swing(InteractionHand.MAIN_HAND);
        return true;
    }
}
