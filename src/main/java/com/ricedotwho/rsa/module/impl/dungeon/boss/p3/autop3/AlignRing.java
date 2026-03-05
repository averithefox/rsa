package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
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
        super(pos, 0.5);
    }

    @Override
    public void run() {
        yaws = null; // need to set for checking if has run
        if (Minecraft.getInstance().player == null || !Minecraft.getInstance().player.onGround()) {
            reset();
            return;
        }

        Vec3 initialVelocity = Minecraft.getInstance().player.getDeltaMovement();
        Vec2 initialDisplacement = MovementPredictor.getDisplacementVector(new Vec2((float) initialVelocity.x, (float) initialVelocity.z));

        Vec3 position = Minecraft.getInstance().player.position();
        Vec3 target = new Vec3(Mth.floor(position.x) + 0.5d, position.y, Mth.floor(position.z) + 0.5d);
        Vec3 delta = target.subtract(position.add(initialDisplacement.x, 0d, initialDisplacement.y));
        double deltaLength = delta.length();

        boolean sneaking = true;
        double displacement = MovementPredictor.getDisplacementFromInput(Minecraft.getInstance().player.getSpeed() * 10, sneaking);

        if (deltaLength < 0.01) {
            return;
        }

        if (deltaLength > 2 * displacement) {
            sneaking = false;
            displacement = MovementPredictor.getDisplacementFromInput(Minecraft.getInstance().player.getSpeed() * 10, sneaking);
            if (deltaLength > 2 * displacement) {
                AutoP3.chat("Too far!");
                reset();
                return;
            }
        }
        KeyMapping.releaseAll();

        double yaw = (float) Math.atan2(-delta.z, delta.x);
        double theta = Math.acos(deltaLength / (2 * displacement));

        yaws = new LinkedList<>();
        yaws.add(new Pair<>((float) -Math.toDegrees(yaw + theta) - 90f, sneaking));
        yaws.add(new Pair<>((float) -Math.toDegrees(yaw - theta) - 90f, sneaking));
    }

    @Override
    public Colour getColour() {
        return Colour.GREEN;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public boolean tick(InputPollEvent event, AutoP3 autoP3) {
        if (yaws == null) {
            return false; // Not run yet
        }

        if (yaws.isEmpty() || Minecraft.getInstance().player == null) {
            return true;
        }

        if (yaws.peek().getB() && !Minecraft.getInstance().player.getLastSentInput().shift()) {
            event.getInputConsumer().accept(new Input(false, false, false, false, false, true, false));
            return false;
        }

        autoP3.setDesync(true);

        Pair<Float, Boolean> entry = yaws.poll();
        Minecraft.getInstance().player.setYRot(entry.getA());
        event.getInputConsumer().accept(new Input(true, false, false, false, false, entry.getB(), false));
        return yaws.isEmpty();
    }
}
