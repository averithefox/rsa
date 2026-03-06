package com.ricedotwho.rsa.utils;

import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.MathUtils;
import com.ricedotwho.rsm.utils.RotationUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
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
    public final double BLOCK_RANGE = 5.7 * 5.7;
    public final double ENTITY_RANGE = 4d;

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

        SwapManager.sendBlockC08(result.getLocation(), result.getDirection(), swing, true);
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

    public void breakBlock(Pos pos, boolean remove, boolean sync) {
        if (faceDistance(pos.asVec3(), mc.player.position().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0)) > BLOCK_RANGE) return;
        Direction dir = closestFace(pos.asVec3(), mc.player.getEyePosition());
        PacketOrderManager.register(PacketOrderManager.STATE.ATTACK, () -> {
            BlockPos bp = pos.asBlockPos();
            SwapManager.sendC07(bp, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, dir, true, sync);
            if (remove) mc.level.setBlock(bp, Blocks.AIR.defaultBlockState(), 0);
        });
    }

    public double faceDistance(Vec3 pos, Vec3 player) {
        double minDist = Double.MAX_VALUE;
        for (Direction face : Direction.values()) {
            double offsetX = 0;
            double offsetY = 0;
            double offsetZ = 0;

            switch (face) {
                case DOWN:
                    offsetY = -0.5;
                    break;
                case UP:
                    offsetY = 0.5;
                    break;
                case NORTH:
                    offsetZ = -0.5;
                    break;
                case SOUTH:
                    offsetZ = 0.5;
                    break;
                case WEST:
                    offsetX = -0.5;
                    break;
                case EAST:
                    offsetX = 0.5;
                    break;
            }

            Vec3 faceVec = pos.add(0.5 + offsetX, 0.5 + offsetY, 0.5 + offsetZ);
            double dist = player.distanceToSqr(faceVec);

            if (dist < minDist) {
                minDist = dist;
            }
        }
        return minDist;
    }

    public Direction closestFace(Vec3 pos, Vec3 player) {
        double minDist = Double.MAX_VALUE;
        Direction closest = Direction.UP;

        for (Direction face : Direction.values()) {
            double offsetX = 0;
            double offsetY = 0;
            double offsetZ = 0;

            switch (face) {
                case DOWN:
                    offsetY = -0.5;
                    break;
                case UP:
                    offsetY = 0.5;
                    break;
                case NORTH:
                    offsetZ = -0.5;
                    break;
                case SOUTH:
                    offsetZ = 0.5;
                    break;
                case WEST:
                    offsetX = -0.5;
                    break;
                case EAST:
                    offsetX = 0.5;
                    break;
            }

            Vec3 faceVec = pos.add(0.5 + offsetX, 0.5 + offsetY, 0.5 + offsetZ);
            double dist = player.distanceToSqr(faceVec);

            if (dist < minDist) {
                minDist = dist;
                closest = face;
            }
        }
        return closest;
    }
}
