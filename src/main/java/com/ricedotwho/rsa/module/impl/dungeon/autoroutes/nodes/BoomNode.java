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
import com.ricedotwho.rsm.utils.render.render3d.type.Circle;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class BoomNode extends Node {
    @Expose
    private final float yaw;
    @Expose
    private final float pitch;

    public BoomNode(Pos localPos, float yaw, float pitch, AwaitManager awaits) {
        super(localPos, awaits);
        this.yaw = yaw;
        this.pitch = pitch;
    }


    @Override
    public boolean run(Pos playerPos) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().level == null) return cancel();

        KeyMapping.releaseAll();
        if (!SwapManager.reserveSwap(Items.TNT)) return cancel();

        ChatUtils.chat("Boom!");
        boolean swap = SwapManager.isDesynced();
        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
            SwapManager.sendBlockC08(yaw, pitch, swap, false);
        });
        return false;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = super.serialize();
        json.addProperty("yaw", yaw);
        json.addProperty("pitch", pitch);
        return json;
    }

    @Override
    public void render() {
        Renderer3D.addTask(new Circle(new Vec3(getRealPos().x, getRealPos().y + 0.2f, getRealPos().z), true, this.getRadius(), Colour.RED, 30));
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
        return new BoomNode(playerRelative, player.getYRot(), player.getXRot(), awaits);
    }
}
