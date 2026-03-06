package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import oshi.util.tuples.Pair;

import java.util.LinkedList;
import java.util.Queue;

public class AlignRing extends Ring {
    private Queue<Pair<Float, Boolean>> yaws;

    public AlignRing(Vec3 pos) {
        this(pos, RingType.ALIGN.getRenderSizeOffset());
    }

    public AlignRing(Vec3 pos, double renderOffset) {
        super(pos, 0.5, renderOffset);
    }

    @Override
    public RingType getType() {
        return RingType.ALIGN;
    }

    @Override
    public boolean run() {
        yaws = null; // need to set for checking if has run
        if (Minecraft.getInstance().player == null || !Minecraft.getInstance().player.onGround()) {
            reset();
            return false;
        }

        Vec3 initialVelocity = Minecraft.getInstance().player.getDeltaMovement();
        Vec2 initialDisplacement = MovementPredictor.getDisplacementVector(new Vec2((float) initialVelocity.x, (float) initialVelocity.z));

        Vec3 position = Minecraft.getInstance().player.position();
        Vec3 boxCenter = this.getBox().getCenter();
        Vec3 target = new Vec3(boxCenter.x, position.y, boxCenter.z);
        Vec3 delta = target.subtract(position.add(initialDisplacement.x, 0d, initialDisplacement.y));
        double deltaLength = delta.length();

        boolean sneaking = true;
        double displacement = MovementPredictor.getDisplacementFromInput(Minecraft.getInstance().player.getSpeed() * 10, sneaking);

        if (deltaLength < 0.01) {
            yaws = new LinkedList<>();
            return false;
        }

        if (deltaLength > 2 * displacement) {
            sneaking = false;
            displacement = MovementPredictor.getDisplacementFromInput(Minecraft.getInstance().player.getSpeed() * 10, sneaking);
            if (deltaLength > 2 * displacement) {
                AutoP3.chat("Too far!");
                reset();
                return false;
            }
        }
        KeyMapping.releaseAll();

        double yaw = (float) Math.atan2(-delta.z, delta.x);
        double theta = Math.acos(deltaLength / (2 * displacement));

        yaws = new LinkedList<>();
        yaws.add(new Pair<>((float) -Math.toDegrees(yaw + theta) - 90f, sneaking));
        yaws.add(new Pair<>((float) -Math.toDegrees(yaw - theta) - 90f, sneaking));
        return false;
    }

    @Override
    public Colour getColour() {
        return Colour.GREEN;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    protected double getPrecision() {
        return 0.01 * 0.01;
    }

    @Override
    public boolean tick(MutableInput mutableInput, Input input, AutoP3 autoP3) {
        if (yaws == null) {
            return Minecraft.getInstance().player != null && !Minecraft.getInstance().player.onGround(); // Not run yet
        }

        if (Minecraft.getInstance().player == null) {
            return true;
        }

        if (yaws.isEmpty()) {
            Vec3 vel = Minecraft.getInstance().player.getDeltaMovement();
            if (vel.x == 0 && vel.z == 0) return true;
            if (vel.lengthSqr() > (0.3 * 0.3d)) return false; // Too high yet, can't stop early

            Vec3 boxCenter = this.getBox().getCenter();
            Vec3 target = new Vec3(boxCenter.x, Minecraft.getInstance().player.position().y, boxCenter.z);
            return Minecraft.getInstance().player.position().distanceToSqr(target) <= getPrecision();
        }

        if (yaws.peek().getB() && !Minecraft.getInstance().player.getLastSentInput().shift()) {
            mutableInput.shift(true);
            return false;
        }

        autoP3.setDesync(true);

        Pair<Float, Boolean> entry = yaws.poll();
        Minecraft.getInstance().player.setYRot(entry.getA());
        mutableInput.shift(entry.getB());
        mutableInput.forward(true);
        return false;
    }
}
