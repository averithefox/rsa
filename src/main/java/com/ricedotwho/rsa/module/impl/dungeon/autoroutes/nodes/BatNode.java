package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.nodes;

import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AwaitManager;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.Node;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import com.ricedotwho.rsm.utils.Utils;
import com.ricedotwho.rsm.utils.render.render3d.type.Circle;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BatNode extends Node {
    public BatNode(Pos localPos, AwaitManager awaits) {
        super(localPos, awaits);
    }

    @Override
    public boolean run(Pos playerPos) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().level == null) return cancel();

        KeyMapping.releaseAll();
        if (!SwapManager.reserveSwap(BatNode::isWitherBlade) && !SwapManager.reserveSwap(Items.ALLIUM)) return cancel();
        if (!hasBatNear(playerPos, Minecraft.getInstance().level)) return cancel();

        boolean swap = SwapManager.isDesynced();
        PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> {
            SwapManager.sendAirC08(player.getYRot(), 90.0f, swap, false);
        });

        return false;
    }

    private boolean hasBatNear(Pos player, ClientLevel level) {
        Vec3 playerPos = player.asVec3();
        AABB aabb = new AABB(playerPos, playerPos).inflate(6.0d, 6.0d, 6.0d);
        return level.getEntitiesOfClass(Bat.class, aabb).stream().anyMatch(bat -> bat.distanceToSqr(playerPos) < 36);
    }

    private static boolean isWitherBlade(ItemStack itemStack) {
        if (itemStack == null) return false;
        String sbId = ItemUtils.getID(itemStack);
        if (sbId.isEmpty()) return false;
        return Utils.equalsOneOf(sbId, "NECRON_BLADE", "SCYLLA", "HYPERION", "VALKYRIE", "ASTRAEA") && ItemUtils.getCustomData(itemStack).getListOrEmpty("ability_scroll").size() == 3;
    }

    private boolean cancel() {
        this.reset();
        return false;
    }

    @Override
    public void render() {
        Renderer3D.addTask(new Circle(this.getRealPos(), true, this.getRadius(), Colour.BLUE, 30));
    }

    @Override
    public int getPriority() {
        return 16;
    }

    @Override
    public String getName() {
        return "bat";
    }

    public static BatNode supply(UniqueRoom fullRoom, LocalPlayer player, AwaitManager awaits) {
        Room mainRoom = fullRoom.getMainRoom();
        Pos playerRelative = RoomUtils.getRelativePosition(new Pos(player.position()), mainRoom);
        return new BatNode(playerRelative, awaits);
    }
}
