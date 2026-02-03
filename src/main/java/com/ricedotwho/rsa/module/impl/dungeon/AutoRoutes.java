package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.Ring;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api.RingAction;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.impl.RotateAction;
import com.ricedotwho.rsm.component.impl.map.Map;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.data.Rotation;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.utils.Accessor;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(aliases = "Autoroutes", id = "Autoroutes", category = Category.DUNGEONS)
public class AutoRoutes extends Module implements Accessor {

    private final List<? extends RingAction> RING_ACTIONS = List.of(
            new RotateAction()
    );

    public final List<Ring> rings = new ArrayList<>();

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null) return;

        if (event.getPacket() instanceof ServerboundMovePlayerPacket packet) {
            if (!packet.hasPosition()) return;

            Room room = Map.getCurrentRoom();
            if (room == null) return;

            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();

            Pos pos = new Pos(x, y, z);
            Vec3 vec = pos.asVec3();

            String roomName = room.getData().name();

            for (Ring ring : rings) {
                if (!ring.roomName.equals(roomName)) continue;

                Pos realPos = RoomUtils.getRealPosition(ring.pos, room);

                AABB aabb = new AABB(
                        realPos.x - ring.widthX / 2,
                        realPos.y,
                        realPos.z - ring.widthZ / 2,
                        realPos.x + ring.widthX / 2,
                        realPos.y + ring.height,
                        realPos.z + ring.widthZ / 2
                );

                if (!aabb.contains(vec)) {
                    ring.active = false;
                    continue;
                }

                if (ring.active) {
                    continue;
                }

                Rotation realRot = RoomUtils.getRealYaw(ring.rot);
                ring.active = ring.action.executeAction(realPos, realRot);
            }
        }
    }

    @Nullable
    public RingAction getAction(String name) {
        return RING_ACTIONS.stream()
                .filter(action -> action.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Ring getClosestRing(Room room, double x, double y, double z) {
        String roomName = room == null ? "" : room.getData().name();
        Vec3 vec = new Vec3(x, y, z);

        Ring closestRing = null;
        double closestDistance = Double.MAX_VALUE;

        for (Ring ring : rings) {
            if (!roomName.isEmpty() && !roomName.equals(ring.roomName)) continue;

            Vec3 ringVec = new Vec3(x, y, z);
            double distance = vec.distanceTo(ringVec);
            if (distance > closestDistance) continue;

            closestDistance = distance;
            closestRing = ring;
        }

        return closestRing;
    }

}