package com.ricedotwho.rsa;

import com.ricedotwho.rsa.module.impl.render.EffectsAndRender;
import com.ricedotwho.rsa.packet.sb.BloodClipHelperStartPacket;
import com.ricedotwho.rsa.packet.sb.BloodClipHelperStopPacket;
import com.ricedotwho.rsa.utils.render3d.type.Ring;
import com.ricedotwho.rsm.addon.Addon;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.utils.ChatUtils;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RSA implements Addon {
  @Getter
  public static boolean notInTestEnv;
  @Getter
  private static final Logger logger = LogManager.getLogger("rsa");
  public static Path SOUNDS_FOLDER;
  @Getter
  private static final MutableComponent prefix = Component.empty()
    .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
    .append(Component.literal("R").withColor(0xB263DF))
    .append(Component.literal("S").withColor(0xC57BEA))
    .append(Component.literal("A").withColor(0xD793F4))
    .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY));

  @Override
  public void onInitialize() {
    // todo: auth prob

    // packet reg
    PayloadTypeRegistry.playC2S().register(BloodClipHelperStartPacket.TYPE, BloodClipHelperStartPacket.CODEC);
    PayloadTypeRegistry.playC2S().register(BloodClipHelperStopPacket.TYPE, BloodClipHelperStopPacket.CODEC);

    EffectsAndRender.init();

    Renderer3D.registerLine(Ring.class);

    SOUNDS_FOLDER = FabricLoader.getInstance()
      .getConfigDir()
      .resolve("rsm")
      .resolve("sounds");

    try {
      Files.createDirectories(SOUNDS_FOLDER);
    } catch (Exception e) {
      e.printStackTrace();
    }

    //server check
    ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
      ClientPacketListener conn = client.getConnection();
      if (conn == null) {
        notInTestEnv = true;
        return;
      }
      String address = conn.getConnection().getRemoteAddress().toString();
      boolean isTestServer = address.contains("hypixelp3sim.zapto.org");
      boolean isSingleplayer = client.hasSingleplayerServer();
      notInTestEnv = !isTestServer && !isSingleplayer;
    });
  }

  @Override
  public void onUnload() {
    // this will never run
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Class<? extends Module>> getModules() {
    ClassLoader cl = RSA.class.getClassLoader();
    try (ScanResult result = new ClassGraph()
      .overrideClassLoaders(cl)
      .acceptPackages("com.ricedotwho.rsa")
      .enableAnnotationInfo()
      .scan()) {
      return (List<Class<? extends Module>>) (List<?>) result
        .getClassesWithAnnotation(ModuleInfo.class)
        .loadClasses(Module.class);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Class<? extends ModComponent>> getComponents() {
    ClassLoader cl = RSA.class.getClassLoader();
    try (ScanResult result = new ClassGraph()
      .overrideClassLoaders(cl)
      .acceptPackages("com.ricedotwho.rsa")
      .enableAnnotationInfo()
      .enableMethodInfo()
      .scan()) {
      return (List<Class<? extends ModComponent>>) (List<?>) result
        .getSubclasses(ModComponent.class)
        .filter(info -> info.hasMethodAnnotation(SubscribeEvent.class))
        .loadClasses(ModComponent.class);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Class<? extends Command>> getCommands() {
    ClassLoader cl = RSA.class.getClassLoader();
    try (ScanResult result = new ClassGraph()
      .overrideClassLoaders(cl)
      .acceptPackages("com.ricedotwho.rsa")
      .enableAnnotationInfo()
      .scan()) {
      return (List<Class<? extends Command>>) (List<?>) result
        .getClassesWithAnnotation(CommandInfo.class)
        .loadClasses(Command.class);
    }
  }

  public static void chat(Object message, Object... objects) {
    ChatUtils.chatClean(getPrefix().copy().append(String.format(message.toString(), objects)));
  }

  public static void chat(String text) {
    ChatUtils.chatClean(getPrefix().copy().append(text));
  }

  public static void chat(Component component) {
    ChatUtils.chatClean(getPrefix().copy().append(component));
  }
}
