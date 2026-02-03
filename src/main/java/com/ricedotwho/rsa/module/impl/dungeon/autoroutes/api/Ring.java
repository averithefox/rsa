package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api;

import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.data.Rotation;
import net.minecraft.world.phys.AABB;

public class Ring {

    public String roomName;
    public RingAction action;

    public Pos pos;
    public Rotation rot;

    public Ring(String roomName, RingAction action, Pos pos, Rotation rot) {
        this.roomName = roomName;
        this.action = action;
        this.pos = pos;
        this.rot = rot;
    }

    public double widthX = 0.5, height = 0.5, widthZ = 0.5;
    public boolean active = false;

    public AABB getBoundingBox(Room room) {
        Pos realPos = RoomUtils.getRealPosition(pos, room);
        if (realPos == null) return null;

        double x = realPos.x;
        double y = realPos.y;
        double z = realPos.z;

        return new AABB(
                x - this.widthX / 2,
                y,
                z - this.widthZ / 2,
                x + this.widthX / 2,
                y + this.height,
                z + this.widthZ / 2
        );
    }

}