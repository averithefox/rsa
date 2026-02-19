package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.module.impl.dungeon.AutoRoutes;
import com.ricedotwho.rsa.module.impl.dungeon.DynamicRoutes;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AutoroutesFileManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;
import com.ricedotwho.rsa.utils.render3d.type.Ring;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.module.impl.movement.Ether;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.EtherUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.Line;
import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class DynamicEtherwarpNode extends EtherwarpNode {

    public DynamicEtherwarpNode(Pos localPos, Pos localTargetPos, AwaitManager awaits, boolean start) {
        super(localPos, localTargetPos, null, false);
    }

    public DynamicEtherwarpNode(Pos localPos, Pos localTargetPos) {
        super(localPos, localTargetPos, null, false);
    }

    @Override
    public void calculate(UniqueRoom room) {
        this.realPos = this.localPos;
        this.realTargetPos = this.localTargetPos;
    }

    @Override
    public JsonObject serialize() {
        // Shouldn't be called
        return new JsonObject();
    }

    @Override
    public String getName() {
        return "dynamicEther";
    }

    @Override
    public Colour getColour() {
        return DynamicRoutes.getNodeColor().getValue();
    }

    public static DynamicEtherwarpNode supply(UniqueRoom fullRoom, LocalPlayer player) {
        Vec3 target = EtherUtils.rayTraceBlock(61, player.getYRot(), player.getXRot(), player.position().add(0d, player.getEyeHeight(Pose.CROUCHING), 0d));
        if (target == null) return null;
        Room mainRoom = fullRoom.getMainRoom();
        Pos playerRelative = RoomUtils.getRelativePosition(new Pos(player.position()), mainRoom);
        Pos targetRelative = RoomUtils.getRelativePosition(new Pos(target), mainRoom);
        return new DynamicEtherwarpNode(playerRelative, targetRelative);
    }
}
