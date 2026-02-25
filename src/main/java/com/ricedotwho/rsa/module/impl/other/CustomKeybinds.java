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
    private final StringSetting string1 = new StringSetting("Command 1", "");
    private final StringSetting string2 = new StringSetting("Command 2", "");
    private final StringSetting string3 = new StringSetting("Command 3", "");
    private final StringSetting string4 = new StringSetting("Command 4", "");
    private final StringSetting string5 = new StringSetting("Command 5", "");
    private final StringSetting string6 = new StringSetting("Command 6", "");
    private final StringSetting string7 = new StringSetting("Command 7", "");
    private final StringSetting string8 = new StringSetting("Command 8", "");
    private final StringSetting string9 = new StringSetting("Command 9", "");
    private final StringSetting string10 = new StringSetting("Command 10", "");
    private final StringSetting string11 = new StringSetting("Command 11", "");
    private final StringSetting string12 = new StringSetting("Command 12", "");
    private final StringSetting string13 = new StringSetting("Command 13", "");
    private final StringSetting string14 = new StringSetting("Command 14", "");
    private final StringSetting string15 = new StringSetting("Command 15", "");

    private final KeybindSetting keybind1 = new KeybindSetting("Keybind 1", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string1.getValue())));
    private final KeybindSetting keybind2 = new KeybindSetting("Keybind 2", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string2.getValue())));
    private final KeybindSetting keybind3 = new KeybindSetting("Keybind 3", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string3.getValue())));
    private final KeybindSetting keybind4 = new KeybindSetting("Keybind 4", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string4.getValue())));
    private final KeybindSetting keybind5 = new KeybindSetting("Keybind 5", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string5.getValue())));
    private final KeybindSetting keybind6 = new KeybindSetting("Keybind 6", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string6.getValue())));
    private final KeybindSetting keybind7 = new KeybindSetting("Keybind 7", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string7.getValue())));
    private final KeybindSetting keybind8 = new KeybindSetting("Keybind 8", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string8.getValue())));
    private final KeybindSetting keybind9 = new KeybindSetting("Keybind 9", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string9.getValue())));
    private final KeybindSetting keybind10 = new KeybindSetting("Keybind 10", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string10.getValue())));
    private final KeybindSetting keybind11 = new KeybindSetting("Keybind 11", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string11.getValue())));
    private final KeybindSetting keybind12 = new KeybindSetting("Keybind 12", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string12.getValue())));
    private final KeybindSetting keybind13 = new KeybindSetting("Keybind 13", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string13.getValue())));
    private final KeybindSetting keybind14 = new KeybindSetting("Keybind 14", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string14.getValue())));
    private final KeybindSetting keybind15 = new KeybindSetting("Keybind 15", new Keybind(GLFW.GLFW_KEY_UNKNOWN, false, () -> sendCmd(string15.getValue())));

    //todo: create a gui for this
    public CustomKeybinds() {
        this.registerProperty(
                keybind1,
                string1,
                keybind2,
                string2,
                keybind3,
                string3,
                keybind4,
                string4,
                keybind5,
                string5,
                keybind6,
                string6,
                keybind7,
                string7,
                keybind8,
                string8,
                keybind9,
                string9,
                keybind10,
                string10,
                keybind11,
                string11,
                keybind12,
                string12,
                keybind13,
                string13,
                keybind14,
                string14,
                keybind15,
                string15
        );
    }

    private void sendCmd(String cmd) {
        if (mc.player == null || mc.getConnection() == null) return;
        mc.getConnection().sendCommand(cmd);
    }
}
