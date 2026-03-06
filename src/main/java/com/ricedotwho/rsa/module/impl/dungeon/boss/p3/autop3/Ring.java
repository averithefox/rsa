package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import com.ricedotwho.rsm.utils.render.render3d.type.OutlineBox;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class Ring{
    @Getter
    private AABB box;
    @Getter
    private AABB renderBox;
    @Setter
    @Getter
    private boolean triggered;


    protected Ring(Vec3 pos, double radius, double renderOffset) {
        this(pos.subtract(radius, 0, radius), pos.add(radius, radius * 2, radius), renderOffset); // Centered at bottom
    }

    protected Ring(Vec3 min, Vec3 max, double renderOffset) {
        this.box = new AABB(min, max);
        this.renderBox = box.contract(renderOffset, renderOffset, renderOffset);
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
        Renderer3D.addTask(new OutlineBox(this.getRenderBox(), getColour(), depth));
    }

    // Run will always run before tick
    public abstract boolean run(); // Return true if can process another ring
    public abstract Colour getColour();
    public abstract int getPriority();
    public abstract boolean tick(MutableInput mutableInput, Input input, AutoP3 autoP3);

}
