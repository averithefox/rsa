package com.ricedotwho.rsa.module.impl.dungeon.autoroutes.api;


import net.minecraft.world.phys.Vec3;

public enum NodeRunState {
    MOVE,
    NO_MOVE,
    FAILED;

    private Vec3 newState;

    public void setState(Vec3 newState) {
        this.newState = newState;
    }

    public Vec3 getState() {
        return newState;
    }
}
