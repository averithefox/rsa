package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsa.RSA;
import com.ricedotwho.rsm.component.impl.map.handler.DungeonInfo;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.RoomType;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ModuleInfo(aliases = {"Instant Clear", "Insta Clear"}, id = "InstaClear")
public class InstantClear extends Module {

    private final float
            MAP_LEFT = -200,
            MAP_RIGHT = 0,
            MAP_TOP = -200,
            MAP_BOTTOM = 0;

    private UniqueRoom targetRoom;

    @SubscribeEvent
    public void onRoomChangeEvent(DungeonEvent.ChangeRoom event) {
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;

        List<String> orderedRooms = BloodBlink.getRooms();

        Set<UniqueRoom> rooms = DungeonInfo.getUniqueRooms()
                .stream()
                .filter(r -> {
                    Room mainRoom = r.getMainRoom();
                    if (mainRoom == null) return false;
                    RoomType type = mainRoom.getData().type();
                    return type == RoomType.NORMAL || type == RoomType.RARE;
                })
                .sorted(Comparator.comparingDouble(r -> orderedRooms.indexOf(r.getName())))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (rooms.isEmpty()) return;

        for (UniqueRoom room : rooms) {
            boolean isEmpty = room.getTiles().stream().allMatch(tile -> {
                int topX = tile.getX();
                int topY = tile.getZ();
                int bottomX = topX + 32;
                int bottomY = topY + 32;

                AABB bounds = new AABB(
                        bottomX, tile.getBottom(), bottomY,
                        topX, tile.getRoofHeight(), topY
                );

                List<Entity> entities = level.getEntities(null, bounds);
                return entities.isEmpty();
            });

            if (!isEmpty) continue;

            targetRoom = room;
            break;
        }
    }

    private Vec2 getOutsideVec(int x, int z) {
        float leftDist = x - MAP_LEFT;
        float rightDist = MAP_RIGHT - x;
        float topDist = z - MAP_TOP;
        float bottomDist = MAP_BOTTOM - z;

        float dx = Math.min(leftDist, rightDist);
        float dz = Math.min(topDist, bottomDist);

        if (dx < dz) {
            return leftDist < rightDist ? new Vec2(MAP_LEFT, z) : new Vec2(MAP_RIGHT, z);
        } else {
            return topDist < bottomDist ? new Vec2(x, MAP_TOP) : new Vec2(x, MAP_BOTTOM);
        }
    }

    private boolean isOutside(int x, int z) {
        return (x < -200 || x > 0) && (z < -200 || z > 0);
    }

}