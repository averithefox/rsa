package com.ricedotwho.rsa.component.impl.managers;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.ricedotwho.rsa.IMixin.IMultiPlayerGameMode;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

public class SwapManager {
    @Getter
    private static int serverSlot = -1;
    private static boolean swappedThisTick = false;

    public static void onPreTickStart() {
        swappedThisTick = false;
    }

    public static boolean onPostSendPacket(Packet<?> packet) {
        if (!(packet instanceof ServerboundSetCarriedItemPacket slotPacket)) return true;
        ChatUtils.chat("Item Packet!");

        if (swappedThisTick || slotPacket.getSlot() == serverSlot) {
            //ChatUtils.chatOfficial("Prevented 0 tick swap!");
            return false;
        }

        swappedThisTick = true;
        serverSlot = slotPacket.getSlot();
        return true;
    }

    public static void sendC08(float yaw, float pitch, boolean syncSlots) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().player.gameMode() == GameType.SPECTATOR) return;

        if (Minecraft.getInstance().gameMode == null || Minecraft.getInstance().level == null) return;
        IMultiPlayerGameMode manager = ((IMultiPlayerGameMode) Minecraft.getInstance().gameMode);

        if (syncSlots) manager.syncSlot();
        manager.sendPacketSequenced(Minecraft.getInstance().level, sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, yaw, pitch));
    }

    public static boolean swapItem(Item item) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || item == null || swappedThisTick || player.getInventory().getItem(player.getInventory().getSelectedSlot()).getItem() == item) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i); // Hotbar is 0 - 8
            if (stack.getItem() != item) continue;
            return swapSlot(i);
        }
        return false;
    }

    public static boolean swapSlot(int slot) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || swappedThisTick) return false;
        if (slot < 0 || slot > 8) {
            System.err.println("Invalid swap slot! : " + slot);
            return false;
        }
        // Might zero tick
        player.getInventory().setSelectedSlot(slot);
        return true;
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
