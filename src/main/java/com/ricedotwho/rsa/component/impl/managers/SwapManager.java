package com.ricedotwho.rsa.component.impl.managers;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.ricedotwho.rsa.IMixin.IMultiPlayerGameMode;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;

public class SwapManager {
    @Getter
    private static int serverSlot;

    private static int lastSentServerSlot;
    private static boolean swappedThisTick = false;

    public static void onPreTickStart() {
        swappedThisTick = false;
    }

    public static boolean onPostSendPacket(Packet<?> packet) {
        if (!(packet instanceof ServerboundSetCarriedItemPacket slotPacket)) return true;

        if (swappedThisTick || slotPacket.getSlot() == lastSentServerSlot) {
            ChatUtils.chat("Prevented packet 0 tick swap! This shouldn't happen, tell hyper!");
            return false;
        }

        swappedThisTick = true;
        serverSlot = slotPacket.getSlot();
        lastSentServerSlot = slotPacket.getSlot();
        return true;
    }

    public static void onHandleLogin() {
        // The Minecraft.MultiPlayerGameMode is reset here, so its server slot is also reset
        serverSlot = 0;
        lastSentServerSlot = 0; // Scary but should be fine
    }

    // Cancels call if returns false
    public static boolean onEnsureHasSentCarriedItem(int managerServerSlot) {
        if (Minecraft.getInstance().player == null) return false;
        if (serverSlot != managerServerSlot) {
            ChatUtils.chat("Slot mismatch! Tell Hyper if you see this!");
            ChatUtils.chat("SwapManger : " + serverSlot);
            ChatUtils.chat("GameMode : " + managerServerSlot);
        }
        int i = Minecraft.getInstance().player.getInventory().getSelectedSlot();
        if (i != managerServerSlot && !swappedThisTick) {
            serverSlot = i;
            return true;
        }
        return false;
    }

    public static boolean sendC08(float yaw, float pitch, boolean syncSlots) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().player.gameMode() == GameType.SPECTATOR) return false;
        if (Minecraft.getInstance().gameMode == null || Minecraft.getInstance().level == null) return false;

        IMultiPlayerGameMode manager = ((IMultiPlayerGameMode) Minecraft.getInstance().gameMode);

        int i = Minecraft.getInstance().player.getInventory().getSelectedSlot();
        if (syncSlots) manager.syncSlot();
        if (syncSlots && !checkServerSlot(i)) {
            ChatUtils.chat("Failed to swap to slot : " + i);
            return false;
        }

        manager.sendPacketSequenced(Minecraft.getInstance().level, sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, yaw, pitch));
        return true;
    }

    public static boolean swapItem(Item item) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || item == null) return false;
        if (item == player.getInventory().getItem(player.getInventory().getSelectedSlot()).getItem()) return true; // Already on this item

        if (swappedThisTick) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i); // Hotbar is 0 - 8
            if (stack.getItem() != item) continue;
            return swapSlot(i);
        }
        return false;
    }

    public static boolean swapSlot(int slot) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (slot == serverSlot) return true;
        if (player == null || swappedThisTick) return false;
        if (slot < 0 || slot > 8) {
            System.err.println("Invalid swap slot! : " + slot);
            return false;
        }

        player.getInventory().setSelectedSlot(slot);
        return true;
    }

    public static boolean checkServerSlot(int slot) {
        return serverSlot == slot;
    }

    public static boolean checkServerItem(Item item) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || serverSlot < 0 || serverSlot > 8) return false;

        ItemStack stack = player.getInventory().getItem(serverSlot);
        return stack.getItem() == item;
    }

    public static boolean checkClientItem(Item item) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;

        ItemStack stack = player.getInventory().getItem(player.getInventory().getSelectedSlot());
        return stack.getItem() == item;
    }

    // TODO
    // Hook these functions at a lower level
}
