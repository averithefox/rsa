package com.ricedotwho.rsa.module.impl.dungeon.terminals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;

public class TerminalRenderer {
    private AbstractContainerMenu terminalContainer;
    private final HashMap<Integer, ItemStack> overrides;
    private boolean overridesUpdated;

    public TerminalRenderer() {
        this.terminalContainer = null;
        this.overrides = new HashMap<>();
        this.overridesUpdated = false;
    }

    public void render(GuiGraphics guiGraphics, float x, float y, Terminal terminal) {
        if (terminalContainer == null || terminalContainer.slots.isEmpty()) return;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);

        int slotCount = getGuiSlotCount(this.terminalContainer.getType());

        boolean bl = terminal instanceof StartsWith || terminal instanceof Colors;
        if (bl && terminal.isSolved())
            tryUpdateOverrides(terminal);

        for (int i = 0; i < slotCount; i++) {
            if (i >= this.terminalContainer.slots.size()) break;
            Slot slot = this.terminalContainer.slots.get(i);
            ItemStack stack = bl && overrides.containsKey(slot.index) ? overrides.get(slot.index) : slot.getItem();

            renderSlot(guiGraphics, slot.x, slot.y, stack);
        }
        guiGraphics.pose().popMatrix();
    }

    // This swaps the select with colors and starts with terms with just panes
    // Only call this if the term type is correct
    private void tryUpdateOverrides(Terminal terminal) {
        if (this.overridesUpdated) return;

        overrides.clear();
        List<Slot> slots = this.terminalContainer.slots;
        int slotCount = getGuiSlotCount(this.terminalContainer.getType());

        for (int i = 0; i < slotCount; i++) {
            if (i >= slots.size()) break;
            Slot slot = slots.get(i);
            ItemStack stack = slot.getItem();

            if (stack.isEmpty() || stack.getItem() == Items.BLACK_STAINED_GLASS_PANE) continue;
            Item item = (terminal.isSolved() && terminal.getSolution().containsIndex(i)) ? Items.RED_STAINED_GLASS_PANE : Items.GREEN_STAINED_GLASS_PANE;
            overrides.put(slot.index, item.getDefaultInstance().copyWithCount(stack.getCount()));
        }

        this.overridesUpdated = true;
    }

    private static void renderSlot(GuiGraphics guiGraphics, int x, int y, ItemStack stack) {
        if (stack.isEmpty()) return;

        int k = x + y * 176;
        guiGraphics.renderItem(stack, x, y, k);
        renderItemCount(guiGraphics, Minecraft.getInstance().font, stack, x, y);
    }

    private static void renderItemCount(GuiGraphics guiGraphics, Font font, ItemStack itemStack, int i, int j) {
        if (itemStack.getCount() != 1) {
            String string2 = String.valueOf(itemStack.getCount());
            guiGraphics.drawString(font, string2, i + 19 - 2 - font.width(string2), j + 6 + 3, -1, true);
        }
    }

    public void newWindow(AbstractContainerMenu menu) {
        this.overridesUpdated = false;
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
