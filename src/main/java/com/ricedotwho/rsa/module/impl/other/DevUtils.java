package com.ricedotwho.rsa.module.impl.other;

import com.ricedotwho.rsa.component.impl.managers.SwapManager;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ButtonSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.datatransfer.Clipboard;

import static net.minecraft.core.Direction.getYRot;

@Getter
@ModuleInfo(aliases = "DevUtils", id = "DevUtils", category = Category.OTHER)
public class DevUtils extends Module {
    private final ButtonSetting pos = new ButtonSetting("Your XYZ", "List Pos", () -> {
        LocalPlayer player = Minecraft.getInstance().player;
        Minecraft mc = Minecraft.getInstance();
        KeyboardHandler keyboard = mc.keyboardHandler;
        if(player == null) return;
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        String xyz = x + ", " + y + ", " + z;
        ChatUtils.chat(xyz);
        keyboard.setClipboard(xyz);
        ChatUtils.chat("Copied to clipboard!");
    });
    private final ButtonSetting yawPitch = new ButtonSetting("Yaw and Pitch", "Yaw/Pitch", () -> {
        LocalPlayer player = Minecraft.getInstance().player;
        Minecraft mc = Minecraft.getInstance();
        KeyboardHandler keyboard = mc.keyboardHandler;
        if(player == null) return;
        float yaw =  player.getYRot();
        float pitch = player.getXRot();
        String yp = yaw + ", " + pitch;
        ChatUtils.chat(yp);
        keyboard.setClipboard(yp);
    });
    private final ButtonSetting blockinfo = new ButtonSetting("Block info that you're lookin at", "Block Info", () -> {
        LocalPlayer player = Minecraft.getInstance().player;
        Minecraft mc = Minecraft.getInstance();
        KeyboardHandler keyboard = mc.keyboardHandler;
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if(player == null) return;

        if(hitResult.getType() == HitResult.Type.BLOCK) {
            String x = String.valueOf(hitResult.getLocation().x);
            String y = String.valueOf(hitResult.getLocation().y);
            String z = String.valueOf(hitResult.getLocation().z);

            String simpleX = String.valueOf(Mth.floor(Float.parseFloat(x)));
            String simpleY = String.valueOf(Mth.floor(Float.parseFloat(y)));
            String simpleZ = String.valueOf(Mth.floor(Float.parseFloat(z)));


            String BlockInfo = x + ", " + y + ", " + z;
            String SblockInfo = simpleX + ", " + simpleY + ", " + simpleZ;;

            ChatUtils.chat("SimpleString: " + SblockInfo);
            ChatUtils.chat("XYZ: " + BlockInfo);
        }
    });
    private final ButtonSetting entityinfo = new ButtonSetting("Entity info that you're lookin at", "Entity Info", () -> {
        LocalPlayer player = Minecraft.getInstance().player;
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if(player == null) return;
        EntityHitResult entityHR = (EntityHitResult) hitResult;
        String entityInfo = entityHR.getEntity().getName().getString();
        String entityId = String.valueOf(entityHR.getEntity().getId());
        String simplePos = entityHR.getEntity().blockPosition().getX() + ", " + entityHR.getEntity().blockPosition().getY() + ", " + entityHR.getEntity().blockPosition().getZ();
        ChatUtils.chat("Name: " + entityInfo);
        ChatUtils.chat("ID: " + entityId);
        ChatUtils.chat("Pos: " + simplePos);
    });

    public DevUtils() {
        this.registerProperty(
                pos,
                yawPitch,
                blockinfo,
                entityinfo
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
}
