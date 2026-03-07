package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsa.module.impl.dungeon.boss.VelocityBuffer;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Colour;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.util.freetype.FreeType;

import java.util.function.Predicate;

public class BonzoRing extends Ring {
    private final float yaw;
    private final float pitch;
    protected byte state;
    protected static final byte END_STATE = 5;

    public BonzoRing(Vec3 pos) {
        super(pos, 0.5, RingType.BONZO.getRenderSizeOffset());
        this.yaw = Minecraft.getInstance().gameRenderer.getMainCamera().yaw();
        this.pitch = Minecraft.getInstance().gameRenderer.getMainCamera().getXRot();
        this.state = 0;
    }

    @Override
    public RingType getType() {
        return RingType.BONZO;
    }

    @Override
    public void reset() {
        super.reset();
        this.state = 0;
    }

    protected void registerWaitCondition() {
        PacketOrderManager.registerReceiveListener((p) -> {
            if (Minecraft.getInstance().player == null || this.state != 1) return true;
            if (!(p instanceof ClientboundSetEntityMotionPacket motionPacket) || motionPacket.getId() != Minecraft.getInstance().player.getId())
                return false;
            this.state = END_STATE;
            return true;
        });
    }


    @Override
    public boolean run() {
        if (Minecraft.getInstance().player == null) return false;

        switch (state) {
            case (0) -> {
                super.reset();
                if (!SwapManager.swapItem("BONZO_STAFF")) return false;
                VelocityBuffer velocityBuffer = RSM.getModule(VelocityBuffer.class);
                if (!velocityBuffer.isEnabled()) velocityBuffer.onKeyToggle();

                PacketOrderManager.register(PacketOrderManager.STATE.ITEM_USE, () -> SwapManager.sendAirC08(yaw, pitch, true));

                state = 1;
                registerWaitCondition();
                return false;
            }

            case (END_STATE) -> {
                return false;
            }

            default -> {
                super.reset();
                return false;
            }
        }
    }

    @Override
    public Colour getColour() {
        return Colour.MAGENTA;
    }

    @Override
    public int getPriority() {
        return 75;
    }

    @Override
    public boolean tick(MutableInput mutableInput, Input input, AutoP3 autoP3) {
        return true;
    }
}
