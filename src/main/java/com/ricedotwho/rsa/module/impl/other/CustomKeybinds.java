package com.ricedotwho.rsa.module.impl.other;

import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.StringSetting;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

@Getter
@ModuleInfo(aliases = "Keybind Shortcuts", id = "CustomKeybinds", category = Category.OTHER)
public class CustomKeybinds extends Module {
    boolean Keypressed1 = false;
    boolean Keypressed2 = false;
    boolean Keypressed3 = false;
    boolean Keypressed4 = false;
    boolean Keypressed5 = false;
    boolean Keypressed6 = false;
    boolean Keypressed7 = false;
    boolean Keypressed8 = false;
    boolean Keypressed9 = false;
    boolean Keypressed10 = false;
    boolean Keypressed11 = false;
    boolean Keypressed12 = false;
    boolean Keypressed13 = false;
    boolean Keypressed14 = false;
    private final KeybindSetting Keybind1 = new KeybindSetting("Keybind 1", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> Keypressed1 = true));
    private final StringSetting String1 = new StringSetting("Command 1 (dont need the /)", "");
    private final KeybindSetting Keybind2 = new KeybindSetting("Keybind 2", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> Keypressed2 = true));
    private final StringSetting String2 = new StringSetting("Command 2", "");
    private final KeybindSetting Keybind3 = new KeybindSetting("Keybind 3", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> Keypressed3 = true));
    private final StringSetting String3 = new StringSetting("Command 3", "");
    private final KeybindSetting Keybind4 = new KeybindSetting("Keybind 4", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> Keypressed4 = true));
    private final StringSetting String4 = new StringSetting("Command 4", "");
    private final KeybindSetting Keybind5 = new KeybindSetting("Keybind 5", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> Keypressed5 = true));
    private final StringSetting String5 = new StringSetting("Command 5", "");
    private final KeybindSetting Keybind6 = new KeybindSetting("Keybind 6", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> Keypressed6 = true));
    private final StringSetting String6 = new StringSetting("Command 6", "");
    private final KeybindSetting Keybind7 = new KeybindSetting("Keybind 7", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> Keypressed7 = true));
    private final StringSetting String7 = new StringSetting("Command 7", "");
    private final KeybindSetting Keybind8 = new KeybindSetting("Keybind 8", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> Keypressed8 = true));
    private final StringSetting String8 = new StringSetting("Command 8", "");
    private final KeybindSetting Keybind9 = new KeybindSetting("Keybind 9", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> Keypressed9 = true));
    private final StringSetting String9 = new StringSetting("Command 9", "");
    private final KeybindSetting Keybind10 = new KeybindSetting("Keybind 10", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> Keypressed10 = true));
    private final StringSetting String10 = new StringSetting("Command 10", "");
    private final KeybindSetting Keybind11 = new KeybindSetting("Keybind 11", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> Keypressed11 = true));
    private final StringSetting String11 = new StringSetting("Command 11", "");
    private final KeybindSetting Keybind12 = new KeybindSetting("Keybind 12", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> Keypressed12 = true));
    private final StringSetting String12 = new StringSetting("Command 12", "");
    private final KeybindSetting Keybind13 = new KeybindSetting("Keybind 13", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> Keypressed13 = true));
    private final StringSetting String13 = new StringSetting("Command 13", "");
    private final KeybindSetting Keybind14 = new KeybindSetting("Keybind 14", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> Keypressed14 = true));
    private final StringSetting String14 = new StringSetting("Command 14", "");
    //these work when you run the client in inteleJ but its not working in my instance.. IM SO CONFUSED



    public CustomKeybinds() {
        this.registerProperty(
                Keybind1,
                String1,
                Keybind2,
                String2,
                Keybind3,
                String3,
                Keybind4,
                String4,
                Keybind5,
                String5,
                Keybind6,
                String6,
                Keybind7,
                String7,
                Keybind8,
                String8,
                Keybind9,
                String9,
                Keybind10,
                String10,
                Keybind11,
                String11,
                Keybind12,
                String12,
                Keybind13,
                String13,
                Keybind14,
                String14
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
    public void sendChat(Render2DEvent event){
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = Minecraft.getInstance().level;
        if(player == null || level == null) return;

        if(Keypressed1){
            player.connection.sendCommand(String1.getValue());
            Keypressed1 = false;
        }

        if(Keypressed2){
            player.connection.sendCommand(String2.getValue());
            Keypressed2 = false;
        }

        if(Keypressed3){
            player.connection.sendCommand(String3.getValue());
            Keypressed3 = false;
        }

        if(Keypressed4){
            player.connection.sendCommand(String4.getValue());
            Keypressed4 = false;
        }

        if(Keypressed5){
            player.connection.sendCommand(String5.getValue());
            Keypressed5 = false;
        }

        if(Keypressed6){
            player.connection.sendCommand(String6.getValue());
            Keypressed6 = false;
        }

        if(Keypressed7){
            player.connection.sendCommand(String7.getValue());
            Keypressed7 = false;
        }

        if(Keypressed8){
            player.connection.sendCommand(String8.getValue());
            Keypressed8 = false;
        }

        if(Keypressed9){
            player.connection.sendCommand(String9.getValue());
            Keypressed9 = false;
        }

        if(Keypressed10){
            player.connection.sendCommand(String10.getValue());
            Keypressed10 = false;
        }

        if(Keypressed11){
            player.connection.sendCommand(String11.getValue());
            Keypressed11 = false;
        }

        if(Keypressed12){
            player.connection.sendCommand(String12.getValue());
            Keypressed12 = false;
        }

        if(Keypressed13){
            player.connection.sendCommand(String13.getValue());
            Keypressed13 = false;
        }

        if(Keypressed14){
            player.connection.sendCommand(String14.getValue());
            Keypressed14 = false;
        }
    }
}
