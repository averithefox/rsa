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
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class AotvNode extends Node {
    @Expose
    private final Pos localRotationVector;
    private Pos realRotationVector;

    public AotvNode(Pos localPos, Pos localRotationVector, AwaitManager awaits) {
        super(localPos, awaits);
        this.localRotationVector = localRotationVector;
        this.realRotationVector = null;
    }

    @Override
    public void calculate(UniqueRoom room) {
        super.calculate(room);
        this.realRotationVector = RoomUtils.getRealPosition(this.localRotationVector, room.getMainRoom());
    }

    @Override
    public boolean run(Pos playerPos) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return cancel();

        KeyMapping.releaseAll();

        AutoRoutes autoRoutes = RSM.getModule(AutoRoutes.class);
        autoRoutes.setForceSneak(false);
        if (!SwapManager.reserveSwap(Items.DIAMOND_SHOVEL)) return cancel();

        if (Minecraft.getInstance().player.getLastSentInput().shift()) {
            return cancel();
        }

        boolean swap = SwapManager.isDesynced();
        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
            if ((swap && !SwapManager.checkClientItem(Items.DIAMOND_SHOVEL)) || (!swap && !SwapManager.checkServerItem(Items.DIAMOND_SHOVEL))) {
                // Swap didn't work??? It got swapped back? WTF
                ChatUtils.chat("Big fuck up! : " + swap + ", " + Minecraft.getInstance().player.getInventory().getItem(SwapManager.getServerSlot()).getItem());
                return;
            }

            float[] angles = EtherUtils.getYawAndPitch(realRotationVector.x, realRotationVector.y, realRotationVector.z);
            if (!SwapManager.sendAirC08(angles[0], angles[1], swap, false)) {
                ChatUtils.chat("Failed to send ether C08!");
                return;
            }
        });

        playerPos.selfAdd(0.0d, player.getEyeHeight(Pose.STANDING), 0.0d).selfAdd(realRotationVector.multiply(12));
        autoRoutes.setForceSneak(true);
        return true;
    }

    @Override
    public void render() {
        Vec3 playerRealPos = this.getRealPos().asVec3();
        Renderer3D.addTask(new Circle(playerRealPos.add(0.0d, 0.1d, 0.0d), true, this.getRadius(), Colour.GREEN, 30));
        //Renderer3D.addTask(new Line(playerRealPos, this.realRotationVector.asVec3(), Colour.CYAN, Colour.CYAN, true));
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = super.serialize();
        json.add("rotationVec", AutoroutesFileManager.gson.toJsonTree(localRotationVector));
        return json;
    }

    @Override
    public int getPriority() {
        return 8; // Slightly lower
    }

    @Override
    public String getName() {
        return "aotv";
    }

    public static AotvNode supply(UniqueRoom fullRoom, LocalPlayer player, AwaitManager awaits) {
        Room mainRoom = fullRoom.getMainRoom();
        Pos playerRelative = RoomUtils.getRelativePosition(new Pos(player.position()), mainRoom);
        Pos targetRelative = RoomUtils.getRelativePosition(new Pos(player.getViewVector(1f)), mainRoom);
        return new AotvNode(playerRelative, targetRelative, awaits);
    }
}
