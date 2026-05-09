package com.ricedotwho.rsa.interfaces;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;

public interface IMultiPlayerGameMode {
  void rsa$sendPacketSequenced(ClientLevel world, PredictiveAction packetCreator);

  void rsa$syncSlot();
}
