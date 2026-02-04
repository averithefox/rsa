package com.ricedotwho.rsa.module.impl.dungeon;

import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ButtonSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

@Getter // please don't use spaces in the id
@ModuleInfo(aliases = "P.T", id = "PadTimer", category = Category.RENDER)
public class PadTimer extends Module {
    private int seconds = 4; // 4 seconds
    private int second = 20; // 20 ticks
    private int padSeconds = 4;// 4 seconds
    private boolean IsEnabled = false;
    private boolean pPadcountdown = false;
    private boolean yPadcountdown = false;
    private boolean pPadcountdownT = false;
    private boolean yPadcountdownT = false;
    private boolean countdownP = false;
    private boolean pPadmsg = false;
    private int stopShowing = 44;
    private int stopShowing2 = 1;
    private boolean restartvalues = false;
    private int pPadTicks = 160; // 160 ticks [8 seconds]
    private int yPadTicks = 40;// 40 ticks [2 seconds]
    private final ButtonSetting test1 = new ButtonSetting("Test Purple Pad Timer", "off", () -> pPadcountdownT = !pPadcountdownT);
    private final ButtonSetting rsvalues = new ButtonSetting("Restart Values", "restartvalues", () -> restartvalues = true);
    private final ButtonSetting test2 = new ButtonSetting("Test Yellow Pad Timer", "off", () -> yPadcountdownT = !yPadcountdownT);
    private final DragSetting PADELERT = new DragSetting("PAD ELERT", new Vector2d(10, 10), new Vector2d(50, 15));

    String string = "Pad in " + seconds;

    public PadTimer() {
        this.registerProperty(
                test1,
                test2,
                PADELERT,
                rsvalues
        );
    }

    @Override
    public void onEnable() {
        IsEnabled = true;
    }

    @Override
    public void onDisable() {
        IsEnabled = false;
    }

    @Override
    public void reset() {
        IsEnabled = false;
    }

    @SubscribeEvent
    public void onChat(ChatEvent event) {
        ChatUtils.chat("test");
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        String unformatted = StringUtil.stripColor(event.getMessage().getString());
        Vec3 pos = player.position();


        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        Item item = itemStack.getItem();

        if(unformatted.contains("I'd be happy to show you what that's like!")) {
            pPadcountdownT = true;
            pPadmsg = true;
            IsEnabled = true;
            ChatUtils.chat("Pad Countdown Started.");
        };


    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {;
        if(Location.getArea() != Island.Dungeon) IsEnabled = true;
        if(!IsEnabled || !pPadcountdownT) return;
        if(pPadcountdownT)countdownP = true;
        if(padSeconds <= 0)countdownP = false;
        ChatUtils.chat(second + " second");
        if(second > 0 && pPadTicks <= 0 && countdownP){
            second--;
            if(second == 0) {ChatUtils.chat("PAD IN: " + padSeconds); padSeconds--; ChatUtils.chat(pPadTicks);}
            if(padSeconds <= 0) countdownP = false;
            return;
        }
        seconds--;
        if(seconds <= 0 && pPadcountdownT) {second = 20;}

        if (stopShowing > 0 && pPadTicks <= 0)  {
            stopShowing--;
            ChatUtils.chat(stopShowing);
            return;
        }

        if(pPadTicks > 0 && pPadcountdownT && countdownP){
            pPadTicks--;
            return;
        }
        if(pPadTicks == 0) {
            pPadTicks = 1;
        }
        pPadcountdownT = false;
    }


    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        if(restartvalues){
            seconds = 4; // 4 seconds
            second = 20; // 20 ticks
            padSeconds = 4;// 4 seconds
            IsEnabled = false;
            pPadcountdown = false;
            yPadcountdown = false;
            pPadcountdownT = false;
            yPadcountdownT = false;
            countdownP = false;
            pPadmsg = false;
            stopShowing = 44;
            stopShowing2 = 1;
            pPadTicks = 160; // 160 ticks [8 seconds]
            yPadTicks = 40;// 40 ticks [2 seconds]
            ChatUtils.chat("Values Restarted.");
            restartvalues = false;
        }
        if (padSeconds <= 0 && stopShowing > stopShowing2 && Location.getArea() == Island.Dungeon) {
//            ChatUtils.chat(pPadTicks);
            this.PADELERT.renderScaled(event.getGfx(), () -> NVGUtils.drawText("PAD NOW!!!", 0, 0, 50f, Colour.blue, NVGUtils.JOSEFIN), 60, 30);
        }
    }
}