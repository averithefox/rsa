package com.ricedotwho.rsa.component.impl;

import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.Accessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlockAura implements Accessor {
    private static final List<BlockData> queue = new ArrayList<>();

    public static boolean addBlock(Pos pos, boolean isSwap, boolean remove) {
        BlockPos bp = pos.asBlockPos();
        if (mc.level.getBlockState(bp).getShape(mc.level, bp).isEmpty()) return false;
        queue.add(new BlockData(pos, isSwap, remove));
        return true;
    }

    public static void addBlock(List<Pos> positions, boolean remove) {
        for (Pos pos : positions) {
            BlockPos bp = pos.asBlockPos();
            if (mc.level.getBlockState(bp).getShape(mc.level, bp).isEmpty()) continue;
            queue.add(new BlockData(pos, false, remove));
        }
    }

    public static void onPreHandleKeybinds() {
        if (queue.isEmpty()) return;
        Vec3 eyes = mc.player.getEyePosition();

        // get first block within 5 blocks
        Optional<BlockData> opt = queue.stream().filter(bd -> eyes.distanceToSqr(bd.block().asVec3()) <= 25).findFirst();

        if (opt.isEmpty()) return;
        BlockData target = opt.get();
        queue.remove(target);

        Direction dir = closestFace(target.block().asVec3(), eyes);

        PacketOrderManager.register(PacketOrderManager.STATE.ATTACK, () -> {
            // idfk what im doing
            BlockPos bp = target.block().asBlockPos();
            SwapManager.sendC07(bp, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, dir, true, target.isSwap());
            // we don't need to send an abort block break as of right now (2/10/2026), may change in the future
            if (target.remove()) mc.level.setBlock(bp, Blocks.AIR.defaultBlockState(), 0); // idk if 0 is the right one
        });
    }

    /// maybe the face should be stored inside the node
    private static Direction closestFace(Vec3 pos, Vec3 player) {
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

    private record BlockData(Pos block, boolean isSwap, boolean remove) { }
}
