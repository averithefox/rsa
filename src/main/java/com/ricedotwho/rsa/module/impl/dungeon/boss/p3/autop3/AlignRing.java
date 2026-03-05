package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.AutoP3;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.render.render3d.type.OutlineBox;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class AlignRing extends Ring {
    public AlignRing(Vec3 pos) {
        super(pos, 1);
    }

    @Override
    public void render(boolean depth) {
        Renderer3D.addTask(new OutlineBox(this.getBox(), getColour(), depth));
    }

    @Override
    public void run() {
        Vec3 initialVelocity = Minecraft.getInstance().player.getDeltaMovement();
        Vec2 initialDisplacement = MovementPredictor.getDisplacementVector(new Vec2((float) initialVelocity.x, (float) initialVelocity.z));

        Vec3 position = Minecraft.getInstance().player.position();
        Vec3 target = new Vec3(Mth.floor(position.x) + 0.5d, position.y, Mth.floor(position.z) + 0.5d);
        Vec3 delta = target.subtract(position.add(initialDisplacement.x, 0d, initialDisplacement.y));
        double deltaLength = delta.length();

        boolean sneaking = true;
        double displacement = MovementPredictor.getDisplacementFromInput(Minecraft.getInstance().player.getSpeed() * 10, sneaking);

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

//        if (Minecraft.getInstance().player.getDeltaMovement().x != 0 || Minecraft.getInstance().player.getDeltaMovement().z != 0 || !Minecraft.getInstance().player.onGround()) {
//            reset();
//            return;
//        }

        if (deltaLength < 0.01) {
            return;
        }


        double yaw = (float) Math.atan2(-delta.z, delta.x);
        double theta = Math.acos(deltaLength / (2 * displacement));

        AutoP3 autoP3 = RSM.getModule(AutoP3.class);
        autoP3.queueYaw((float) -Math.toDegrees(yaw + theta) - 90f, sneaking);
        autoP3.queueYaw((float) -Math.toDegrees(yaw - theta) - 90f, sneaking);
    }

    @Override
    public Colour getColour() {
        return Colour.GREEN;
    }

    @Override
    public int getPriority() {
        return 100;
    }
}
