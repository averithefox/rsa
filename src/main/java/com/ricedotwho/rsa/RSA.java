package com.ricedotwho.rsa;

import com.ricedotwho.rsa.command.impl.BloodBlinkCommand;
import com.ricedotwho.rsa.command.impl.DynamicRouteCommand;
import com.ricedotwho.rsa.command.impl.RSADevCommand;
import com.ricedotwho.rsa.module.impl.dungeon.boss.InstaMid;
import com.ricedotwho.rsa.module.impl.dungeon.boss.PadTimer;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autoterms.AutoTerms;
import com.ricedotwho.rsa.module.impl.dungeon.FastLeap;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.TermAura;
import com.ricedotwho.rsa.module.impl.dungeon.device.AlignAura;
import com.ricedotwho.rsa.module.impl.dungeon.device.Auto4;
import com.ricedotwho.rsa.module.impl.dungeon.device.AutoSS;
import com.ricedotwho.rsa.module.impl.dungeon.puzzle.AutoTTT;
import com.ricedotwho.rsa.module.impl.dungeon.puzzle.Puzzles;
import com.ricedotwho.rsa.module.impl.other.*;
import com.ricedotwho.rsa.module.impl.dungeon.*;
import com.ricedotwho.rsa.command.impl.RouteCommand;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AutoroutesFileManager;
import com.ricedotwho.rsa.module.impl.player.CancelInteract;
import com.ricedotwho.rsa.module.impl.render.EffectsAndRender;
import com.ricedotwho.rsa.module.impl.render.Esp;
import com.ricedotwho.rsa.module.impl.render.HidePlayers;
import com.ricedotwho.rsa.module.impl.render.PresetWaypoints;
import com.ricedotwho.rsa.utils.render3d.type.Ring;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.addon.Addon;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.module.Module;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RSA implements Addon {

    @Getter
    private static final Logger logger = LogManager.getLogger("rsa");
    public static Path SOUNDS_FOLDER;

    @Override
    public void onInitialize() {
        // todo: auth prob
        EffectsAndRender.init();

        AutoroutesFileManager.init(RSM.getModule(AutoRoutes.class));
        AutoroutesFileManager.load();

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
                SessionLogin.class,
                AutoTerms.class,
                InstaMid.class,
                SecretHitboxes.class,
                CancelInteract.class,
                DevUtils.class,
                CancelInteract.class,
                TermAura.class,
                Esp.class,
                AlignAura.class,
                AntiCheat.class,
                FastLeap.class,
                AutoTTT.class,
                AntiCheat.class,
                IceFill.class,
                Puzzles.class,
                HidePlayers.class,
                Auto4.class
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
                RSADevCommand.class
        );
    }

}