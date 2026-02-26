package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class Ring{
    @Getter
    private AABB box;
    @Setter
    @Getter
    private boolean triggered;


    protected Ring(Vec3 pos, double radius) {
        this(pos.subtract(radius, 0, radius), pos.add(radius, radius * 2, radius)); // Centered at bottom
    }

    protected Ring(Vec3 min, Vec3 max) {
        this.box = new AABB(min, max);
        this.triggered = false;
    }

    public boolean isInNode(Vec3 playerPos) {
        return playerPos.x >= box.minX && playerPos.x <= box.maxX && playerPos.y >= box.minY && playerPos.y <= box.maxY && playerPos.z >= box.minZ && playerPos.z <= box.maxZ;
    }

    public boolean updateState(Vec3 playerPos) {
        boolean bl = isInNode(playerPos);

        if (bl && !this.triggered) {
            // Trigger will be set later
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

    public abstract void render(boolean depth);
    public abstract void run();
    public abstract Colour getColour();
    public abstract int getPriority();


}
