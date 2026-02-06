package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api;

import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.data.Rotation;
import lombok.Getter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class Node {
    private final Pos localPos;
    private final float r;
    private boolean triggered;
    private int lastTickTime;

    @Getter
    private Pos realPos;

    public Node(Pos localPos) {
        this(localPos, 0.5f);
    }

    public Node(Pos localPos, float r) {
        this.localPos = localPos;
        this.r = r;

        this.triggered = false;
        this.lastTickTime = -1;
    }

    public void calculate(UniqueRoom room) {
        this.realPos = RoomUtils.getRealPosition(this.localPos, room.getMainRoom());
    }

    public abstract boolean run(Pos playerPos);
    public abstract void render();

    public float getRadius() {
        return r;
    }

    public int getPriority() {
        return 8;
    }

    public boolean isInNode(Pos playerPos) {
        return playerPos.squaredDistanceTo(this.realPos) <= r * r;
    }

    public boolean updateNodeState(Pos playerPos, int tickTime) {
        if (tickTime <= lastTickTime) return false; // Don't go do the same node twice in 1 tick, also blocks from setting it to untriggered
        boolean bl = isInNode(playerPos);
        if (bl && !this.triggered) {
            this.triggered = true;
            this.lastTickTime = tickTime;
            return true;
        }

        if (!bl && this.triggered) {
            reset();
        }
        return false;
    }

    public void reset() {
        this.triggered = false;
    }


}