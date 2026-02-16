package com.ricedotwho.rsa.module.impl.other;

import com.ricedotwho.rsa.module.impl.other.Checks.invWalk;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ricedotwho.rsa.module.impl.other.Checks.invWalk.setRunning;
import static com.ricedotwho.rsa.module.impl.other.Checks.invWalk.terminalCompletedMsg;

@Getter
@ModuleInfo(aliases = "AntiCheat", id = "AntiCheat", category = Category.OTHER)
public class AntiCheat extends Module {
    public static final BooleanSetting termWalking = new BooleanSetting("Terminal Walking", false, () -> true);
    private static final Pattern playerName = Pattern.compile("^(\\w+)\\s+activated a terminal");

    public AntiCheat() {
        this.registerProperty(
                termWalking

        );
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void reset() {

    }

    @SubscribeEvent
    public void InvWalk(Render3DEvent.Extract event){
        if(!termWalking.getValue()) return;
        setRunning(); invWalk.Check1();
    }

    @SubscribeEvent
    public void InvWalk2(ChatEvent event){
        if(!termWalking.getValue()) return;
        terminalCompletedMsg(event);
    }
}
