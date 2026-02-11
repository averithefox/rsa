package com.ricedotwho.rsa.module.impl.dungeon.autoroutes;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.ricedotwho.rsm.component.impl.map.map.UniqueRoom;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.data.Pos;
import lombok.Getter;
import lombok.Setter;

public abstract class Node {
    @Expose
    private final Pos localPos;
    @Expose
    private final float r;
    @Expose
    @Getter
    private final AwaitManager awaitManager;

    @Getter
    @Setter
    private boolean triggered;
    @Getter
    private int lastTickTime;

    @Getter
    private Pos realPos;

    public Node(Pos localPos) {
        this(localPos, null);
    }

    public Node(Pos localPos, AwaitManager awaitManager) {
        this(localPos, awaitManager, 0.5f);
    }

    public Node(Pos localPos, AwaitManager awaitManager, float r) {
        this.localPos = localPos;
        this.r = r;
        this.awaitManager = awaitManager;

        this.triggered = false;
        this.lastTickTime = -1;
    }

    public boolean hasAwaits() {
        return this.awaitManager != null && this.awaitManager.hasAwaits();
    }

    public boolean shouldAwait() {
        return this.awaitManager != null && this.awaitManager.shouldAwait();
    }

    public void calculate(UniqueRoom room) {
        this.realPos = RoomUtils.getRealPosition(this.localPos, room.getMainRoom());
        if (this.hasAwaits()) this.getAwaitManager().resetAwaits();
    }

    public abstract boolean run(Pos playerPos);
    public abstract void render();

    protected boolean cancel() {
        this.reset();
        return false;
    }

    public float getRadius() {
        return r;
    }

    public int getPriority() {
        return 8;
    }

    public boolean isInNode(Pos playerPos) {
        return playerPos.squaredDistanceTo(this.realPos) <= r * r;
    }

    public void updateLastTickTime(int lastTickTime) {
        this.lastTickTime = lastTickTime;
    }

    public boolean hasRanThisTick(int tickTime) {
        return (tickTime <= lastTickTime);
    }

    public void preTrigger(int tickTime) {
        this.lastTickTime = tickTime;
        this.triggered = true;
    }

    public boolean updateNodeState(Pos playerPos, int tickTime) {
        if (tickTime <= lastTickTime) return false; // Don't go do the same node twice in 1 tick, also blocks from setting it to untriggered
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

    public abstract String getName();

    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("type", this.getName());
        json.add("localPos", AutoroutesFileManager.gson.toJsonTree(localPos));
        json.addProperty("radius", r);
        if (this.awaitManager == null || !this.awaitManager.hasAwaits()) return json;
        json.add("awaits", this.awaitManager.serialize());
        return json;
    }

    public void reset() {
        this.triggered = false;
    }


}