package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AutoroutesFileManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.EtherUtils;
import com.ricedotwho.rsm.utils.RotationUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.Circle;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledOutlineBox;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BoomNode extends Node {
    @Expose
    private final Pos target;
    private Pos realTargetPosition;
    private AABB renderAABB;

    public BoomNode(Pos localPos, Pos target, AwaitManager awaits) {
        super(localPos, awaits);
        this.target = target;
        this.realTargetPosition = null;
        this.renderAABB = null;
    }

    @Override
    public void calculate(UniqueRoom room) {
        super.calculate(room);
        this.realTargetPosition = RoomUtils.getRealPosition(this.target, room.getMainRoom());
        this.renderAABB = new AABB(realTargetPosition.x - 0.1f, realTargetPosition.y - 0.1f, realTargetPosition.z  - 0.1f, realTargetPosition.x + 0.1f, realTargetPosition.y + 0.1f, realTargetPosition.z  + 0.1f);
    }


    @Override
    public boolean run(Pos playerPos) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().level == null) return cancel();

        KeyMapping.releaseAll();
        if (!SwapManager.reserveSwap("INFINITE_SUPERBOOM_TNT", "SUPERBOOM_TNT")) return cancel();

        Vec3 eyePos = Minecraft.getInstance().player.position().add(0.0d, EtherUtils.SNEAK_EYE_HEIGHT, 0.0d);
        Vec3 targetVec = realTargetPosition.asVec3();

        boolean swap = SwapManager.isDesynced();
        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {


            BlockPos blockPos = BlockPos.containing(targetVec);
//            System.out.println(targetVec);
//            System.out.println(blockPos);
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockPos);
            if (blockState.getBlock() == Blocks.AIR) return;
            VoxelShape voxelShape = blockState.getShape(Minecraft.getInstance().level, blockPos);
            if (voxelShape.isEmpty()) return;
            AABB blockAABB = voxelShape.bounds();

            Vec3 center = new Vec3((blockAABB.minX + blockAABB.maxX) * 0.5 + blockPos.getX(), (blockAABB.minY + blockAABB.maxY) * 0.5 + blockPos.getY(), (blockAABB.minZ + blockAABB.maxZ) * 0.5 + blockPos.getZ());

//            ChatUtils.chat(targetVec);
//            ChatUtils.chat(blockPos);
//            ChatUtils.chat(eyePos);
//            ChatUtils.chat(center);
            BlockHitResult result = RotationUtils.collisionRayTrace(blockPos, blockAABB, eyePos, center);
            if (result == null) {
                ChatUtils.chat("Failed to find block hit result!");
                return;
            }
            SwapManager.sendBlockC08(result, swap, false);
        });
        return false;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = super.serialize();
        json.add("target", AutoroutesFileManager.gson.toJsonTree(target));
        return json;
    }

    @Override
    public void render() {
        Renderer3D.addTask(new Circle(new Vec3(getRealPos().x, getRealPos().y + 0.2f, getRealPos().z), true, this.getRadius(), Colour.RED, 30));
        Renderer3D.addTask(new FilledOutlineBox(this.renderAABB, Colour.RED.brighter(), Colour.RED.darker(), true));
    }

    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public String getName() {
        return "boom";
    }

    public static BoomNode supply(UniqueRoom fullRoom, LocalPlayer player, AwaitManager awaits) {
        Room mainRoom = fullRoom.getMainRoom();
        Pos playerRelative = RoomUtils.getRelativePosition(new Pos(player.position()), mainRoom);
        if (!(Minecraft.getInstance().hitResult instanceof BlockHitResult blockHitResult) || blockHitResult.getType() == HitResult.Type.MISS) {
            return null;
        }

//        Vec3 target = EtherUtils.rayTraceBlock((int) Math.ceil(player.getContainerInteractionRange()), player.getYRot(), player.getXRot(), playerEyePos);
//        if (target == null) {
//            ChatUtils.chat("No block hit!");
//        }
//        Vec3 dir = target.normalize();
//        target = target.add(dir.scale(EtherUtils.EPSILON));
//        if (target.distanceToSqr(playerEyePos) > 25) {
//            ChatUtils.chat("Too far!");
//        }
        Vec3 eyePos = player.position().add(0d, EtherUtils.SNEAK_EYE_HEIGHT, 0d);
        Vec3 dir = blockHitResult.getLocation().subtract(eyePos).normalize().scale(EtherUtils.EPSILON);
        Pos pos = new Pos(blockHitResult.getLocation());
        pos.selfAdd(dir.x, dir.y, dir.z);
        return new BoomNode(playerRelative, RoomUtils.getRelativePosition(pos, mainRoom), awaits);
    }
}

//*package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes;
//
//import com.google.gson.annotations.Expose;
//import com.ricedotwho.rsa.component.impl.BlockAura;
//import com.ricedotwho.rsa.component.impl.managers.SwapManager;
//import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitManager;
//import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;
//import com.ricedotwho.rsm.component.impl.Renderer3D;
//import com.ricedotwho.rsm.component.impl.map.map.Room;
//import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
//import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
//import com.ricedotwho.rsm.data.Colour;
//import com.ricedotwho.rsm.data.Pos;
//import com.ricedotwho.rsm.utils.EtherUtils;
//import com.ricedotwho.rsm.utils.render.render3d.type.Circle;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.player.LocalPlayer;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.phys.Vec3;
//
//public class BoomNode extends Node {
//    public BoomNode(Pos localPos, AwaitManager awaits, Pos target) {
//        super(localPos, awaits);
//        this.target = target;
//    }
//
//    @Expose
//    private final Pos target;
//    private Pos realTarget = null;
//
//    @Override
//    public void calculate(UniqueRoom room) {
//        super.calculate(room);
//        this.realTarget = RoomUtils.getRealPosition(this.target, room.getMainRoom());
//    }
//
//    @Override
//    public boolean run(Pos playerPos) {
//        if (!SwapManager.reserveSwap("INFINITE_SUPERBOOM_TNT", "SUPERBOOM_TNT")) return cancel(); // b bb but what if they have a game breaker!!
//        BlockAura.addBlock(this.realTarget, true, false);
//        return false;
//    }
//
//    private boolean cancel() {
//        this.reset();
//        return false;
//    }
//
//    @Override
//    public void render() {
//        Renderer3D.addTask(new Circle(this.getRealPos(), true, this.getRadius(), Colour.RED, 30));
//    }
//
//    @Override
//    public int getPriority() {
//        return 20;
//    }
//
//    @Override
//    public String getName() {
//        return "boom";
//    }
//
//    public static BoomNode supply(UniqueRoom fullRoom, LocalPlayer player, AwaitManager awaits) {
//        // surely this is like the looked at block
//        Vec3 target = EtherUtils.rayTraceBlock(5, player.getYRot(), player.getXRot(), player.position().add(0d, player.getEyeHeight(Minecraft.getInstance().player.getPose()), 0d));
//        if (target == null) return null;
//        Room mainRoom = fullRoom.getMainRoom();
//        Pos playerRelative = RoomUtils.getRelativePosition(new Pos(player.position()), mainRoom);
//        Pos targetRelative = RoomUtils.getRelativePosition(new Pos(target), mainRoom);
//        return new BoomNode(playerRelative, awaits, targetRelative);
//    }
//}
