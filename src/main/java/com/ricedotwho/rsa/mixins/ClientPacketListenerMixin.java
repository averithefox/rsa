package com.ricedotwho.rsa.mixins;

import com.mojang.authlib.GameProfile;
import com.ricedotwho.rsa.interfaces.IClientPacketListener;
import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin implements IClientPacketListener {
  @Shadow
  private @Nullable LevelLoadTracker levelLoadTracker;

  @Shadow
  @Final
  private GameProfile localGameProfile;

  @Shadow
  @Final
  private RegistryAccess.Frozen registryAccess;

  @Shadow
  @Final
  private FeatureFlagSet enabledFeatures;

  @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/ClientPacketListener;)V"))
  public void onHandleLogin(CallbackInfo ci) {
    SwapManager.onHandleLogin();
  }

  public CommonListenerCookie rsa$getCookie() {
    ClientPacketListener packetListener = (ClientPacketListener) (Object) this;
    return new CommonListenerCookie(
      this.levelLoadTracker,
      this.localGameProfile,
      null,
      this.registryAccess,
      this.enabledFeatures,
      packetListener.serverBrand(),
      packetListener.getServerData(),
      null,
      Collections.emptyMap(),
      null,
      Collections.emptyMap(),
      null,
      Collections.emptyMap(),
      true
    );
  }
}
