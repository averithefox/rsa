package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.component.impl.pathfinding.PathNode;
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

public class DynamicEtherwarpNode extends Node {
    private final float yaw;
    private final float pitch;
    private Vec3 target;

    public DynamicEtherwarpNode(Pos localPos, float yaw, float pitch, AwaitManager awaits, boolean start) {
        super(localPos, null, false);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public DynamicEtherwarpNode(Pos localPos, float yaw, float pitch) {
        this(localPos, yaw, pitch, null, false);
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

        Pos playerCopy = playerPos.add(0.0d, EtherUtils.SNEAK_EYE_HEIGHT, 0.0d);

        boolean swap = SwapManager.isDesynced();
        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
            if ((swap && !SwapManager.checkClientItem(Items.DIAMOND_SHOVEL)) || (!swap && !SwapManager.checkServerItem(Items.DIAMOND_SHOVEL))) {
                // Swap didn't work??? It got swapped back? WTF
                ChatUtils.chat("Big fuck up! : " + swap + ", " + Minecraft.getInstance().player.getInventory().getItem(SwapManager.getServerSlot()).getItem());
                return;
            }

            if (!SwapManager.sendAirC08(this.yaw, this.pitch, swap, false)) {
                ChatUtils.chat("Failed to send dyn ether C08!");
                return;
            }
        });

        BlockPos etherPos = EtherUtils.fastGetEtherFromOrigin(playerCopy.asVec3(), this.yaw, this.pitch, 61);
        if (etherPos == null) return false;

        playerPos.x = etherPos.getX() + 0.5d;
        playerPos.y = etherPos.getY() + 1.05d; // Fuck you hypixel for the 0.05d
        playerPos.z = etherPos.getZ() + 0.5d;
        return true;
    }

    @Override
    public void calculate(UniqueRoom room) {
        this.realPos = this.localPos;
        this.target = EtherUtils.rayTraceBlock(61, yaw, pitch, this.localPos.add(0d, EtherUtils.SNEAK_EYE_HEIGHT + 0.05, 0d).asVec3());
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
    public void render(boolean depth) {
        Vec3 playerRealPos = this.getRealPos().asVec3();
        Colour colour = this.getColour();
        Renderer3D.addTask(new Ring(playerRealPos, depth, this.getRadius(), colour));
        Renderer3D.addTask(new Line(playerRealPos, this.target, colour, colour, true));
    }


    @Override
    public Colour getColour() {
        return DynamicRoutes.getNodeColor().getValue();
    }

    public static DynamicEtherwarpNode fromPathNode(PathNode node, float yaw, float pitch) {
        Pos nodePos = new Pos(node.getPos().getBottomCenter()).selfAdd(0d, 1d, 0d);
        return new DynamicEtherwarpNode(nodePos, yaw, pitch);
    }

    public static DynamicEtherwarpNode supply(UniqueRoom fullRoom, LocalPlayer player) {
        // Won't work properly when adding manually because not 0.05 blocks off of ground
        Room mainRoom = fullRoom.getMainRoom();
        Pos playerRelative = RoomUtils.getRelativePosition(new Pos(player.position()), mainRoom);
        return new DynamicEtherwarpNode(playerRelative, player.getYRot(), player.getXRot());
    }
}
