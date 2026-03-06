package com.ricedotwho.rsa;

import com.ricedotwho.rsa.command.impl.*;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AutoRoutes;
import com.ricedotwho.rsa.module.impl.dungeon.boss.BreakerAura;
import com.ricedotwho.rsa.module.impl.dungeon.boss.VelocityBuffer;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p4.InstaMid;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p2.PadTimer;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.AutoP3;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.AutoTerms;
import com.ricedotwho.rsa.module.impl.dungeon.FastLeap;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.TermAura;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.solver.TerminalSolver;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p5.Relics;
import com.ricedotwho.rsa.module.impl.dungeon.croesus.AutoCroesus;
import com.ricedotwho.rsa.module.impl.dungeon.device.AlignAura;
import com.ricedotwho.rsa.module.impl.dungeon.device.Auto4;
import com.ricedotwho.rsa.module.impl.dungeon.device.AutoSS;
import com.ricedotwho.rsa.module.impl.dungeon.puzzle.Puzzles;
import com.ricedotwho.rsa.module.impl.other.*;
import com.ricedotwho.rsa.module.impl.dungeon.*;
import com.ricedotwho.rsa.module.impl.player.BonzoHelper;
import com.ricedotwho.rsa.module.impl.player.CancelInteract;
import com.ricedotwho.rsa.module.impl.render.*;
import com.ricedotwho.rsa.packet.sb.BloodClipHelperStartPacket;
import com.ricedotwho.rsa.packet.sb.BloodClipHelperStopPacket;
import com.ricedotwho.rsa.utils.render3d.type.Ring;
import com.ricedotwho.rsm.addon.Addon;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RSA implements Addon {

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

        //AutoroutesFileManager.init(RSM.getModule(AutoRoutes.class));
        //AutoroutesFileManager.load();

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
    }

    @Override
    public void onUnload() {
        // this will never run
    }

    @Override
    public List<Class<? extends Module>> getModules() {
        return List.of(
                DungeonBreaker.class,
                AutoRoutes.class,
                DynamicRoutes.class,
                AutoJax.class,
                PadTimer.class,
                BloodBlink.class,
                AutoSS.class,
                SecretAura.class,
                EffectsAndRender.class,
                PresetWaypoints.class,
                CustomKeybinds.class,
                AutoGfs.class,
                AutoTerms.class,
                InstaMid.class,
                SecretHitboxes.class,
                CancelInteract.class,
                DevUtils.class,
                CancelInteract.class,
                TermAura.class,
                Esp.class,
                AlignAura.class,
                FastLeap.class,
                AntiCheat.class,
                Puzzles.class,
                HidePlayers.class,
                Auto4.class,
                BonzoHelper.class,
                AutoCroesus.class,
                AutoP3.class,
                Freecam.class,
                AutoUlt.class,
                TerminalSolver.class,
                Relics.class,
                VelocityBuffer.class,
                BreakerAura.class
        );
    }

    @Override
    public List<Class<? extends ModComponent>> getComponents() {
        return List.of();
    }

    @Override
    public List<Class<? extends Command>> getCommands() {
        return List.of(
                RouteCommand.class,
                DynamicRouteCommand.class,
                BloodBlinkCommand.class,
                BBGCommand.class,
                RSADevCommand.class,
                AutoCroesusCommand.class,
                SecretAuraCommand.class
        );
    }

    public static void chat(Object message, Object ... objects) {
        ChatUtils.chatClean(getPrefix().copy().append(String.format(message.toString(), objects)));
    }

    public static void chat(String text) {
        ChatUtils.chatClean(getPrefix().copy().append(text));
    }

    public static void chat(Component component) {
        ChatUtils.chatClean(getPrefix().copy().append(component));
    }

}