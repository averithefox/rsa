package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.Expose;
import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.module.impl.dungeon.AutoRoutes;
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
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.EtherUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import lombok.Setter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

public class UseNode extends Node {
    @Expose
    private final Pos localRotationVector;
    @Expose
    private final String itemID;
    @Expose
    @Setter
    private boolean sneak;

    private Pos realRotationVector;

    public UseNode(Pos localPos, Pos localRotationVector, String itemID, boolean sneak, AwaitManager awaits, boolean start) {
        super(localPos, awaits, start);
        this.localRotationVector = localRotationVector;
        this.itemID = itemID;
        this.sneak = sneak;
        this.realRotationVector = null;
    }

    @Override
    public void calculate(UniqueRoom room) {
        super.calculate(room);
        this.realRotationVector = RoomUtils.rotateRealFixed(this.localRotationVector, room.getRotation());
    }

    @Override
    public boolean run(Pos playerPos) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return cancel();

        KeyMapping.releaseAll();

        AutoRoutes autoRoutes = RSM.getModule(AutoRoutes.class);
        autoRoutes.setForceSneak(!sneak);
        if (!SwapManager.reserveSwap(this.itemID)) return cancel();

        if (Minecraft.getInstance().player.getLastSentInput().shift() != sneak) {
            return cancel();
        }

        boolean swap = SwapManager.isDesynced();
        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
            if ((swap && !SwapManager.checkClientItem(this.itemID)) || (!swap && !SwapManager.checkServerItem(this.itemID))) {
                // Swap didn't work??? It got swapped back? WTF
                ChatUtils.chat("Big fuck up! : " + swap + ", " + Minecraft.getInstance().player.getInventory().getItem(SwapManager.getServerSlot()).getItem());
                return;
            }

            float[] angles = EtherUtils.getYawAndPitch(realRotationVector.x, realRotationVector.y, realRotationVector.z);
            if (!SwapManager.sendAirC08(angles[0], angles[1], swap, false)) {
                ChatUtils.chat("Failed to send use C08!");
                return;
            }
        });

        playerPos.selfAdd(0.0d, player.getEyeHeight(Pose.STANDING), 0.0d).selfAdd(realRotationVector.multiply(12));
        autoRoutes.setForceSneak(!sneak);
        return true;
    }

    @Override
    public void render(boolean depth) {
        Vec3 playerRealPos = this.getRealPos().asVec3();
        Renderer3D.addTask(new Ring(playerRealPos.add(0.0d, 0.1d, 0.0d), depth, this.getRadius(), this.getColour()));
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = super.serialize();
        json.add("rotationVec", AutoroutesFileManager.gson.toJsonTree(localRotationVector));
        json.add("itemID", new JsonPrimitive(this.itemID));
        json.add("sneak", new JsonPrimitive(this.sneak));
        return json;
    }

    @Override
    public int getPriority() {
        return 8; // Slightly lower
    }

    @Override
    public String getName() {
        return "use";
    }

    @Override
    public Colour getColour() {
        return this.isStart() ? AutoRoutes.getStartColour().getValue() : AutoRoutes.getUseColour().getValue();
    }

    public static UseNode supply(UniqueRoom fullRoom, LocalPlayer player, AwaitManager awaits, boolean start) {
        Room mainRoom = fullRoom.getMainRoom();
        Pos playerRelative = RoomUtils.getRelativePosition(new Pos(player.position()), mainRoom);
        Pos targetRelative = RoomUtils.rotateRelativeFixed(new Pos(player.getViewVector(1f)), fullRoom.getRotation());
        String itemID = ItemUtils.getID(Minecraft.getInstance().player.getInventory().getSelectedItem());
        if (itemID.isBlank()) return null;
        return new UseNode(playerRelative, targetRelative, itemID, false, awaits, start);
    }
}
