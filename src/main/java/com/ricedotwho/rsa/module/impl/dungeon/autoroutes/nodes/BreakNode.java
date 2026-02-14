package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.module.impl.dungeon.AutoRoutes;
import com.ricedotwho.rsa.module.impl.dungeon.DungeonBreaker;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AutoroutesFileManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;
import com.ricedotwho.rsa.utils.render3d.type.Ring;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.EtherUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledBox;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class BreakNode extends Node implements Accessor {
    public BreakNode(Pos localPos, AwaitManager awaits, boolean start) {
        super(localPos, awaits, start);
        this.blocks = new ArrayList<>();
    }

    @Getter
    @Expose
    private final List<Pos> blocks;
    private List<Pos> rotated = null;
    private boolean running = false;

    @Override
    public void calculate(UniqueRoom room) {
        super.calculate(room);
        rotated = blocks.stream().map(pos -> RoomUtils.getRealPosition(pos, room.getMainRoom())).toList();
    }

    @Override
    public boolean run(Pos playerPos) {
        if (!SwapManager.reserveSwap("DUNGEONBREAKER")) return cancel();
        if (running) return cancel();

        List<Pos> f = rotated.stream().filter(p -> {
            BlockPos bp = p.asBlockPos();
            BlockState state = mc.level.getBlockState(bp);
            VoxelShape shape = state.getShape(mc.level, bp);
            return !shape.isEmpty() && DungeonBreaker.canInstantMine(state) && p.squaredDistanceTo(mc.player.getEyePosition()) < 26;
        }).toList();

        if (f.isEmpty()) return true;
        running = true;

        for (int i = 0; i < f.size(); i++) {
            Pos block = f.get(i);
            TaskComponent.onTick(i, () -> breakBlock(block, true, SwapManager.isDesynced()));
        }

        TaskComponent.onTick(f.size(), () -> running = false);
        return cancel();
    }

    public boolean cancel() {
        this.reset();
        return false;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = super.serialize();
        json.add("blocks", AutoroutesFileManager.gson.toJsonTree(this.blocks));
        return json;
    }

    @Override
    public void render(boolean depth) {
        Renderer3D.addTask(new Ring(this.getRealPos().asVec3(), depth, this.getRadius(), this.getColour()));
        if (this.rotated == null || this.rotated.isEmpty()) return;
        Colour colour = AutoRoutes.getBreakColour().getValue().alpha(90);
        for (Pos pos : rotated) {
            BlockPos bp = pos.asBlockPos();
            BlockState state = mc.level.getBlockState(bp);
            VoxelShape shape = state.getShape(mc.level, bp);
            if (shape.isEmpty()) continue;
            AABB aabb = shape.bounds().move(bp);
            Renderer3D.addTask(new FilledBox(aabb, colour, true));
        }
    }

    @Override
    public int getPriority() {
        return 18;
    }

    @Override
    public String getName() {
        return "break";
    }

    @Override
    public Colour getColour() {
        return this.isStart() ? AutoRoutes.getStartColour().getValue() : AutoRoutes.getBreakColour().getValue();
    }

    public static BreakNode supply(UniqueRoom fullRoom, LocalPlayer player, AwaitManager awaits, boolean start) {
        Room mainRoom = fullRoom.getMainRoom();
        Pos playerRelative = RoomUtils.getRelativePosition(new Pos(player.position()), mainRoom);
        return new BreakNode(playerRelative, awaits, start);
    }

    public void addOrRemoveBlock() {
        if (Map.getCurrentRoom() == null) {
            ChatUtils.chat(ChatFormatting.RED + "Room is null!");
        }

        if (!(Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() == HitResult.Type.MISS) {
            ChatUtils.chat(ChatFormatting.RED + "Not looking at a block");
            return;
        }

        Vec3 eyePos = mc.player.position().add(0d, EtherUtils.SNEAK_EYE_HEIGHT, 0d);
        Vec3 dir = blockHitResult.getLocation().subtract(eyePos).normalize().scale(EtherUtils.EPSILON);
        Pos pos = new Pos(blockHitResult.getLocation());
        pos.selfAdd(dir.x, dir.y, dir.z);

        Pos relPos = RoomUtils.getRelativePosition(pos.floor(), Map.getCurrentRoom().getUniqueRoom().getMainRoom());

        if (blocks.contains(relPos)) {
            blocks.remove(relPos);
            ChatUtils.chat(ChatFormatting.RED + "Removed " + relPos.toChatString() + " from break node");
        } else {
            this.blocks.add(relPos);
            ChatUtils.chat(ChatFormatting.GREEN + "Added " + relPos.toChatString() + " to break node!");
        }
        this.calculate(Map.getCurrentRoom().getUniqueRoom());
    }

    //todo: move to another class
    public static void breakBlock(Pos pos, boolean remove, boolean sync) {
        Direction dir = closestFace(pos.asVec3(), mc.player.getEyePosition());
        PacketOrderManager.register(PacketOrderManager.STATE.ATTACK, () -> {
            BlockPos bp = pos.asBlockPos();
            SwapManager.sendC07(bp, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, dir, true, sync);
            if (remove) mc.level.setBlock(bp, Blocks.AIR.defaultBlockState(), 0);
        });
    }

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
}
