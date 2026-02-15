package com.ricedotwho.rsa.module.impl.other;

import com.ricedotwho.rsa.component.impl.managers.SwapManager;
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
    private boolean worldLoaded = false;
    private boolean countDownstarted = false;
    private int worldLoadTicks = 80;
    private int globalDelay = 0;

    private final BooleanSetting
            enderPearl = new BooleanSetting("EnderPearl", false, () -> true),
            spiritLeap = new BooleanSetting("SpiritLeap", false, () -> true),
            superBoom = new BooleanSetting("SuperBoom", false, () -> true);

    public AutoGfs() {
        this.registerProperty(
                enderPearl,
                spiritLeap,
                superBoom
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
    public void onTick(ClientTickEvent.Start event){
        if(Location.getArea() == Island.Unknown) return;
        if (!worldLoaded) return;
        
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) return;

        if(globalDelay > 0){
            globalDelay--;
            return;
        }

        boolean sentCommand = false;
        if (!sentCommand && enderPearl.getValue()) {
            if (tryGetEnderPearls(player)) {
                globalDelay = 40;
                sentCommand = true;
            }
        }

        if (!sentCommand && spiritLeap.getValue()) {
            if (tryGetSpiritLeaps(player)) {
                globalDelay = 40;
                sentCommand = true;
            }
        }

        if (!sentCommand && superBoom.getValue()) {
            if (tryGetSuperBooms(player)) {
                globalDelay = 40;
            }
        }
    }

    private boolean tryGetEnderPearls(LocalPlayer player) {
        int enderPearlStackSize = 0;
        try {
            ItemStack stack = findItemStackMatching(Items.ENDER_PEARL);
            if (stack != null) {
                enderPearlStackSize = stack.getCount();
            }
        } catch (Exception e) {
            return false;
        }

        // Get ender pearls
        if (enderPearlStackSize > 0 && enderPearlStackSize < 16) {
            int missingAmount = 16 - enderPearlStackSize;
            player.connection.sendCommand("gfs ender_pearl " + missingAmount);
            ChatUtils.chat("GFS ep: " + missingAmount);
            return true;
        }
        return false;
    }

    private boolean tryGetSpiritLeaps(LocalPlayer player) {
        int spiritLeapSlot = SwapManager.getItemSlot("SPIRIT_LEAP");
        if (spiritLeapSlot == -1) return false;

        int spiritLeapCount = 0;
        try {
            ItemStack itemStack = player.getInventory().getItem(spiritLeapSlot);
            if (itemStack != null && !itemStack.isEmpty()) {
                spiritLeapCount = itemStack.getCount();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        // Get spirit leaps
        if(spiritLeapCount > 0 && spiritLeapCount < 16){
            int missingAmount = 16 - spiritLeapCount;
            player.connection.sendCommand("gfs spirit_leap " + missingAmount);
            ChatUtils.chat("GFS sl: " + missingAmount);
            return true;
        }
        return false;
    }

    private boolean tryGetSuperBooms(LocalPlayer player) {
        int superBoomSlot = SwapManager.getItemSlot("SUPERBOOM_TNT");
        if (superBoomSlot == -1) return false; // Item not in hotbar
        
        int superBoomCount = 0;
        try {
            Item superBoomStack = player.getInventory().getItem(superBoomSlot).getItem();
            ItemStack itemStack = findItemStackMatching(superBoomStack);
            if (itemStack != null) {
                superBoomCount = itemStack.getCount();
            }
        } catch (Exception e){
            return false;
        }

        // Get super booms
        if(superBoomCount > 0 && superBoomCount < 64){
            int missingAmount = 64 - superBoomCount;
            player.connection.sendCommand("gfs superboom_tnt " + missingAmount);
            ChatUtils.chat("GFS sb: " + missingAmount);
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void worldLoad(WorldEvent.Load event){
        countDownstarted = true;
        worldLoadTicks = 80;
        globalDelay = 80;
    }

    @SubscribeEvent
    public void countDown(ServerTickEvent event) {
        if(Location.getArea() == Island.Unknown) return;
        if (countDownstarted) {
            worldLoaded = false;
            if (worldLoadTicks > 0) {
                worldLoadTicks--;
                return;
            }
            countDownstarted = false;
            worldLoaded = true;
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