package com.ricedotwho.rsa;

import com.ricedotwho.rsa.command.impl.BloodBlinkCommand;
import com.ricedotwho.rsa.module.impl.other.*;
import com.ricedotwho.rsa.module.impl.dungeon.*;
import com.ricedotwho.rsa.command.impl.RouteCommand;
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AutoroutesFileManager;
import com.ricedotwho.rsa.module.impl.movement.NoRotate;
import com.ricedotwho.rsa.module.impl.player.CancelInteract;
import com.ricedotwho.rsa.module.impl.render.EffectsAndRender;
import com.ricedotwho.rsa.module.impl.render.PresetWaypoints;
import com.ricedotwho.rsa.utils.render3d.type.Ring;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.addon.Addon;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.module.Module;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class RSA implements Addon {

    @Getter
    private static final Logger logger = LogManager.getLogger("rsa");

    @Override
    public void onInitialize() {
        // todo: auth prob
        EffectsAndRender.init();

        AutoroutesFileManager.init(RSM.getModule(AutoRoutes.class));
        AutoroutesFileManager.load();

        Renderer3D.registerLine(Ring.class);
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
                AutoJax.class,
                PadTimer.class,
                BloodBlink.class,
                NoRotate.class,
                AutoSS.class,
                SecretAura.class,
                EffectsAndRender.class,
                PresetWaypoints.class,
                CustomKeybinds.class,
                AutoGfs.class,
                ssidLogin.class,
                AutoTerms.class,
                InstaMid.class,
                SecretHitboxes.class,
                CancelInteract.class
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
                BloodBlinkCommand.class
        );
    }

}