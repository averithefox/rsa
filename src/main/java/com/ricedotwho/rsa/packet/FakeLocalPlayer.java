package com.ricedotwho.rsa.packet;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Input;

public class FakeLocalPlayer extends LocalPlayer {
  public FakeLocalPlayer(Minecraft minecraft, ClientLevel clientLevel, ClientPacketListener clientPacketListener, StatsCounter statsCounter, ClientRecipeBook clientRecipeBook, Input input, boolean bl) {
    super(minecraft, clientLevel, clientPacketListener, statsCounter, clientRecipeBook, input, bl);
  }

  @Override
  public boolean isControlledCamera() {
    return true;
  }
}
