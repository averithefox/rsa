package com.ricedotwho.rsa.IMixin;

import net.minecraft.network.protocol.Packet;

public interface IConnection {
    void receivePacket(Packet<?> packet);
}
