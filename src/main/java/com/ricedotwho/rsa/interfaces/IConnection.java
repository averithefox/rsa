package com.ricedotwho.rsa.interfaces;

import net.minecraft.network.protocol.Packet;

public interface IConnection {
  void rsa$sendPacketImmediately(Packet<?> packet);
}
