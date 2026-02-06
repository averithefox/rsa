package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.nodes;

import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.module.impl.dungeon.AutoRoutes;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.Node;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.NodeRunState;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.EtherUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledOutlineBox;
import com.ricedotwho.rsm.utils.render.render3d.type.OutlineBox;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.compress.archivers.zip.ScatterZipOutputStream;

public class EtherwarpNode extends Node {
    private final Pos localTargetPos;
    private Pos realTargetPos;

    public EtherwarpNode(Pos localPos, Pos localTargetPos) {
        super(localPos);
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
        if (!SwapManager.swapItem(Items.DIAMOND_SHOVEL)) return cancel();

        // Should be put in the other loop if 0 ticking works properly
        AutoRoutes autoRoutes = RSM.getModule(AutoRoutes.class);
        autoRoutes.setForceSneak(true);

        if (!player.getLastSentInput().shift()) {
            return cancel();
        }

        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
        if (!SwapManager.checkClientItem(Items.DIAMOND_SHOVEL)) {
            // Swap didn't work??? It got swapped back? WTF
            ChatUtils.chat("Big fuck up!");
            return;
        }

        float[] angles = EtherUtils.getYawAndPitch(this.realTargetPos.x - playerPos.x, this.realTargetPos.y - (playerPos.y + player.getEyeHeight(Pose.CROUCHING)), this.realTargetPos.z - playerPos.z);
        SwapManager.sendAirC08(angles[0], angles[1], true, false);
        });

        // By this point we assume the etherwarp will work
        BlockPos etherPos = this.realTargetPos.asBlockPos();

        ChatUtils.chat("Found etherBlock : " + etherPos);
        playerPos.x = etherPos.getX() + 0.5d;
        playerPos.y = etherPos.getY() + 1d;
        playerPos.z = etherPos.getZ() + 0.5d;
        return true;
    }

    private boolean cancel() {
        this.reset();
        return false;
    }

    @Override
    public void render() {
        AABB aabb = new AABB(this.getRealPos().subtract(0.5d, 0d, 0.5d).asVec3(), this.getRealPos().add(0.5d, 0.25d, 0.5d).asVec3());
        Renderer3D.addTask(new OutlineBox(aabb, Colour.GREEN, false));
    }

    @Override
    public int getPriority() {
        return 5; // Slightly lower
    }

    public static EtherwarpNode supply(UniqueRoom fullRoom, LocalPlayer player) {
        Vec3 target = EtherUtils.rayTraceBlock(60, player.getYRot(), player.getXRot(), player.position().add(0d, player.getEyeHeight(Pose.CROUCHING), 0d));
        if (target == null) return null;
        Room mainRoom = fullRoom.getMainRoom();
        Pos playerRelative = RoomUtils.getRelativePosition(new Pos(player.position()), mainRoom);
        Pos targetRelative = RoomUtils.getRelativePosition(new Pos(target), mainRoom);
        return new EtherwarpNode(playerRelative, targetRelative);
    }
}
