package com.ricedotwho.rsa.module.impl.other;

import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.ServerTickEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Getter
@ModuleInfo(aliases = "AutoGfs", id = "AutoGfs", category = Category.OTHER)
public class AutoGfs extends Module {
    private final BooleanSetting gfs = new BooleanSetting("GFS", false, () -> true);
    private boolean worldLoaded = false;
    private boolean countDownstarted = false;
    private boolean Checked = false;
    private int worldLoadTicks = 80;
    private int checkTime = 40;
    private int getDelay = 40;

    public AutoGfs() {
        this.registerProperty(
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
    public void CheckPearls(ServerTickEvent event) {
        if(Location.getArea() == Island.Unknown) return;
        if (!worldLoaded) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (checkTime > 0) {
            checkTime--;
            //ChatUtils.chat("Checking: " + checkTime);
            return;
        }Checked = true;

        int enderPearlStackSize;
        try {enderPearlStackSize = findItemStackMatching(Items.ENDER_PEARL).getCount();}
        catch (Exception e) {return;}

        if (enderPearlStackSize == 0) return;
        int missingAmount = 16 - enderPearlStackSize;
        if (missingAmount != 0 && missingAmount != -1) return;
        //ChatUtils.chat(Checked);
        checkTime = 40;
    }

    @SubscribeEvent
    public void getPearls(ClientTickEvent.Start event){
        if(Location.getArea() == Island.Unknown) return;
        if (!Checked) return;
        if (!worldLoaded) return;
        if(getDelay > 0){getDelay--;return;}
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) return;
        int enderPearlStackSize;
        try {enderPearlStackSize = findItemStackMatching(Items.ENDER_PEARL).getCount();}
        catch (Exception e) {return;}

        if (enderPearlStackSize == 0) return;
        int missingAmount = 16 - enderPearlStackSize;
        if(missingAmount == 0)return;
        player.connection.sendCommand("gfs ender_pearl " + missingAmount);
        getDelay = 40;
    }

    @SubscribeEvent
    public void worldLoad(WorldEvent.Load event){
        countDownstarted = true;
        worldLoadTicks = 80;
        checkTime = 40;
        getDelay = 40;
    }

    @SubscribeEvent
    public void countDown(ServerTickEvent event) {
        if(Location.getArea() == Island.Unknown) return;
        if (countDownstarted) {
            worldLoaded = false;
            if (worldLoadTicks > 0) {
                if (worldLoadTicks == 80) ChatUtils.chat("World loading");
                if (worldLoadTicks == 80) ChatUtils.chat("4");
                if (worldLoadTicks == 60) ChatUtils.chat("3");
                if (worldLoadTicks == 40) ChatUtils.chat("2");
                if (worldLoadTicks == 20) ChatUtils.chat("1");
                worldLoadTicks--;
                //ChatUtils.chat("World loading: " + worldLoadTicks);
                return;
            }
            countDownstarted = false;
            ChatUtils.chat("World loaded");
            worldLoaded = true;
            //ChatUtils.chat(worldLoaded);
        }
    }

    public static ItemStack findItemStackMatching(Item item) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || item == null) return null;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i); // Hotbar is 0 - 8
            if (stack.getItem() != item) continue;
            return stack;
        }
        return null;
    }
}