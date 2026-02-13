package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.module.impl.dungeon.AutoRoutes;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AutoroutesFileManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.EtherUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.Circle;
import com.ricedotwho.rsm.utils.render.render3d.type.Line;
import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class EtherwarpNode extends Node {
    @Expose
    private final Pos localTargetPos;
    @Getter
    private Pos realTargetPos;

    public EtherwarpNode(Pos localPos, Pos localTargetPos, AwaitManager awaits) {
        super(localPos, awaits);
        this.localTargetPos = localTargetPos;
        this.realTargetPos = null;
    }

    @Override
    public void calculate(UniqueRoom room) {
        super.calculate(room);
        this.realTargetPos = RoomUtils.getRealPosition(this.localTargetPos, room.getMainRoom());
    }

    @Override
    public boolean run(Pos playerPos) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return cancel();

        KeyMapping.releaseAll();

        if (!SwapManager.reserveSwap(Items.DIAMOND_SHOVEL)) return cancel();

        if (!Minecraft.getInstance().player.getLastSentInput().shift()) {
            return cancel();
        }

        // Hypixel uses old sneak height to find etherwarp position (2 packets ago)
        Pos playerCopy = playerPos.add(0.0d, RSM.getModule(AutoRoutes.class).willBeCrouchingForEtherwarpEvaluation() ? EtherUtils.SNEAK_EYE_HEIGHT : EtherUtils.STAND_EYE_HEIGHT, 0.0d);
        //ChatUtils.chat(playerCopy);
        Pos targetDirection = this.realTargetPos.subtract(playerCopy);
        Pos targetDeltaCopy = targetDirection.copy();

        boolean swap = SwapManager.isDesynced();
        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
            if ((swap && !SwapManager.checkClientItem(Items.DIAMOND_SHOVEL)) || (!swap && !SwapManager.checkServerItem(Items.DIAMOND_SHOVEL))) {
                // Swap didn't work??? It got swapped back? WTF
                ChatUtils.chat("Big fuck up! : " + swap + ", " + Minecraft.getInstance().player.getInventory().getItem(SwapManager.getServerSlot()).getItem());
                return;
            }

            float[] angles = EtherUtils.getYawAndPitch(targetDeltaCopy.x, targetDeltaCopy.y, targetDeltaCopy.z);
            if (!SwapManager.sendAirC08(angles[0], angles[1], swap, false)) {
                ChatUtils.chat("Failed to send ether C08!");
                return;
            }
            //ChatUtils.chat("Sent ether C08! + " + angles[0] + ", " + angles[1]);
            //ChatUtils.chat(angles[0] + ", " + angles[1]);
        });

        // By this point we assume the etherwarp will work
        targetDirection.normalize();
        BlockPos etherPos = this.realTargetPos.add(targetDirection.multiply(EtherUtils.EPSILON)).asBlockPos();

        playerPos.x = etherPos.getX() + 0.5d;
        playerPos.y = etherPos.getY() + 1.05d; // Fuck you hypixel for the 0.05d
        playerPos.z = etherPos.getZ() + 0.5d;
        return true;
    }

    @Override
    public void render(boolean depth) {
        Vec3 playerRealPos = this.getRealPos().asVec3();
        Colour colour = this.getColour();
        Renderer3D.addTask(new Circle(playerRealPos, depth, this.getRadius(), colour, 30));
        Renderer3D.addTask(new Line(playerRealPos, this.realTargetPos.asVec3(), colour, colour, true));
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = super.serialize();
        json.add("localTarget", AutoroutesFileManager.gson.toJsonTree(localTargetPos));
        return json;
    }

    @Override
    public int getPriority() {
        return 5; // Slightly lower
    }

    @Override
    public String getName() {
        return "etherwarp";
    }

    @Override
    public Colour getColour() {
        return this.isStart() ? AutoRoutes.getStartColour().getValue() : AutoRoutes.getEtherwarpColour().getValue();
    }

    public static EtherwarpNode supply(UniqueRoom fullRoom, LocalPlayer player, AwaitManager awaits) {
        // Should use client side eye height so it ray traces to the correct block, this may mean some angles fail server side but atleast it will go where you are trying to
        Vec3 target = EtherUtils.rayTraceBlock(61, player.getYRot(), player.getXRot(), player.position().add(0d, player.getEyeHeight(Pose.CROUCHING), 0d));
        if (target == null) return null;
        Room mainRoom = fullRoom.getMainRoom();
        Pos playerRelative = RoomUtils.getRelativePosition(new Pos(player.position()), mainRoom);
        Pos targetRelative = RoomUtils.getRelativePosition(new Pos(target), mainRoom);
        //ChatUtils.chat("Relative : " + playerRelative);
        return new EtherwarpNode(playerRelative, targetRelative, awaits);
    }
}
