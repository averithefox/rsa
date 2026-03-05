package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import com.ricedotwho.rsm.utils.render.render3d.type.OutlineBox;
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

    public float getDistanceSq(Vec3 vec3) {
        float dx = (float) (((this.box.maxX + this.box.minX) / 2f) - vec3.x);
        float dy = (float) (((this.box.maxY + this.box.minY) / 2f) - vec3.y);
        float dz = (float) (((this.box.maxZ + this.box.minZ) / 2f) - vec3.z);
        return dx * dx + dy * dy + dz * dz;
    }

    public void reset() {
        this.triggered = false;
    }

    public void render(boolean depth) {
        Renderer3D.addTask(new OutlineBox(this.getBox(), getColour(), depth));
    }

    // Run will always run before tick
    public abstract void run();
    public abstract Colour getColour();
    public abstract int getPriority();
    public abstract boolean tick(InputPollEvent event, AutoP3 autoP3);

}
