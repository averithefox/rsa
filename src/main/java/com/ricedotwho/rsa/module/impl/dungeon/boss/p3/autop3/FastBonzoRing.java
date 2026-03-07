package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3;

import com.ricedotwho.rsa.component.impl.managers.PacketOrderManager;
import com.ricedotwho.rsm.data.Colour;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.world.phys.Vec3;

public class FastBonzoRing extends BonzoRing {
    public FastBonzoRing(Vec3 pos) {
        super(pos);
    }

    @Override
    protected void registerWaitCondition() {
        PacketOrderManager.registerReceiveListener((p) -> {
            if (Minecraft.getInstance().player == null || this.state < 1) return true;
            if (!(p instanceof ClientboundPingPacket))
                return false;
            this.state++;
            return this.state >= BonzoRing.END_STATE;
        });
    }

    @Override
    public Colour getColour() {
        return Colour.PINK;
    }

}
