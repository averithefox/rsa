package com.ricedotwho.rsa.component.impl.managers;

import com.ricedotwho.rsa.IMixin.IMultiPlayerGameMode;
import com.ricedotwho.rsa.RSA;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

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

    public static boolean sendAirC08(float yaw, float pitch, boolean syncSlots, boolean swing) {
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
        if (swing) Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
        return true;
    }


    public static boolean sendAirC08(float yaw, float pitch, boolean syncSlots) {
        return sendAirC08(yaw, pitch, syncSlots, false);
    }

    public static void sendBlockC08(BlockHitResult result, boolean swing) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().player.gameMode() == GameType.SPECTATOR) return;
        if (Minecraft.getInstance().gameMode == null || Minecraft.getInstance().level == null) return;

        ((IMultiPlayerGameMode) Minecraft.getInstance().gameMode).sendPacketSequenced(Minecraft.getInstance().level, sequence -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, result, sequence));
        if (swing) Minecraft.getInstance().player.swing(InteractionHand.MAIN_HAND);
    }

    // Haven't implement syncSlots because I haven't found the need
    public static void sendBlockC08(Vec3 pos, Direction direction, boolean swing) {
        sendBlockC08(new BlockHitResult(pos, direction, BlockPos.containing(pos), false), swing);
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

    /// Swap to an item with the specified SkyBlock ID
    public static boolean swapItem(String sbId) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || sbId == null || sbId.isBlank()) return false;
        if (sbId.equals(ItemUtils.getID(player.getInventory().getItem(player.getInventory().getSelectedSlot())))) return true;

        if (swappedThisTick) return false;
        for (int i = 0; i < 9; i++) {
            String id = ItemUtils.getID(player.getInventory().getItem(i));
            if (!sbId.equals(id)) continue;
            return swapSlot(i);
        }
        return false;
    }

    public static boolean swapItem(Predicate<ItemStack> predicate) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;
        if (predicate.test(player.getInventory().getItem(player.getInventory().getSelectedSlot()))) return true;

        if (swappedThisTick) return false;
        for (int i = 0; i < 9; i++) {
            if (!predicate.test(player.getInventory().getItem(i))) continue;
            return swapSlot(i);
        }
        return false;
    }

    public static boolean swapSlot(int slot) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (slot == serverSlot) return true;
        if (player == null || swappedThisTick) return false;
        if (slot < 0 || slot > 8) {
            RSA.getLogger().error("Invalid swap slot! : {}", slot);
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

    public static boolean checkServerItem(String sbId) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || serverSlot < 0 || serverSlot > 8) return false;

        String heldId = ItemUtils.getID(player.getInventory().getItem(serverSlot));
        return sbId.equals(heldId);
    }

    public static boolean checkClientItem(Item item) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;

        ItemStack stack = player.getInventory().getItem(player.getInventory().getSelectedSlot());
        return stack.getItem() == item;
    }

    public static boolean checkClientItem(String sbId) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || sbId.isBlank()) return false;

        String heldId = ItemUtils.getID(player.getInventory().getItem(player.getInventory().getSelectedSlot()));
        return sbId.equals(heldId);
    }

    // TODO
    // Hook these functions at a lower level
}
