package com.ricedotwho.rsa.module.impl.dungeon.terminals;

import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TerminalRenderer {
    private AbstractContainerMenu terminalContainer;

    public TerminalRenderer() {

    }

    public void render(GuiGraphics guiGraphics, int x, int y) {
        if (terminalContainer == null || terminalContainer.slots.isEmpty()) return;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);

        int slotCount = getGuiSlotCount(this.terminalContainer.getType());
        for (int i = 0; i < slotCount; i++) {
            if (i >= terminalContainer.slots.size()) break;

            renderSlot(guiGraphics, terminalContainer.slots.get(i));
        }
        guiGraphics.pose().popMatrix();
    }

    public int getRowCount() {
        if (this.terminalContainer.getType() == MenuType.GENERIC_9x5) return 5;
        if (this.terminalContainer.getType() == MenuType.GENERIC_9x6) return 6;
        return 4;
    }

    private static void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot.getItem().isEmpty()) return;

        int k = slot.x + slot.y * 176;
        guiGraphics.renderItem(slot.getItem(), slot.x, slot.y, k);
        renderItemCount(guiGraphics, Minecraft.getInstance().font, slot.getItem(), slot.x, slot.y);
    }

    private static void renderItemCount(GuiGraphics guiGraphics, Font font, ItemStack itemStack, int i, int j) {
        if (itemStack.getCount() != 1) {
            String string2 = String.valueOf(itemStack.getCount());
            guiGraphics.drawString(font, string2, i + 19 - 2 - font.width(string2), j + 6 + 3, -1, true);
        }
    }

    public void newWindow(AbstractContainerMenu menu) {
        this.terminalContainer = menu;
    }

    public void close() {
        this.terminalContainer = null;
    }


    public static int getGuiSlotCount(MenuType<?> menuType) {
        if (menuType == MenuType.GENERIC_9x4) return 36;
        if (menuType == MenuType.GENERIC_9x5) return 45;
        if (menuType == MenuType.GENERIC_9x6) return 54;
        return -1;
    }

}
