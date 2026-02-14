package com.ricedotwho.rsa.module.impl.dungeon.terminals;

import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Rubix extends Terminal {

    protected Rubix(ClientboundOpenScreenPacket packet) {
        super(TerminalType.RUBIX, packet);
    }
    private static final Item[] COLOR_ORDER = {Items.BLUE_STAINED_GLASS_PANE, Items.RED_STAINED_GLASS_PANE, Items.ORANGE_STAINED_GLASS_PANE, Items.YELLOW_STAINED_GLASS_PANE, Items.GREEN_STAINED_GLASS_PANE};

    @Override
    public void solve() {
        super.solve();

        List<Integer> rubixSlots = new ArrayList<>();

        for (int i = 0; i < this.items.length; i++) {
            ItemStack stack = this.items[i];

            if (stack == null || stack.isEmpty()) continue;
            if (stack.getItem() == Items.BLACK_STAINED_GLASS_PANE) continue;

            if (!isRubixPane(stack.getItem())) continue;
            rubixSlots.add(i);
        }

        int minIndex = -1;
        int minTotal = Integer.MAX_VALUE;

        for (int targetIndex = 0; targetIndex < COLOR_ORDER.length; targetIndex++) {

            int totalClicks = 0;

            for (Integer slot : rubixSlots) {
                ItemStack stack = this.items[slot];

                int currentIndex = indexOf(COLOR_ORDER, stack.getItem());

                int clockwise = (targetIndex - currentIndex + COLOR_ORDER.length) % COLOR_ORDER.length;
                int counterClockwise = (currentIndex - targetIndex + COLOR_ORDER.length) % COLOR_ORDER.length;

                totalClicks += Math.min(clockwise, counterClockwise);
            }

            if (totalClicks < minTotal) {
                minTotal = totalClicks;
                minIndex = targetIndex;
            }
        }

        List<SolutionClick> solutionClicks = new ArrayList<>();

        for (Integer slot : rubixSlots) {

            ItemStack stack = this.items[slot];
            int currentIndex = indexOf(COLOR_ORDER, stack.getItem());

            int clockwise = (minIndex - currentIndex + COLOR_ORDER.length) % COLOR_ORDER.length;
            int counterClockwise = (currentIndex - minIndex + COLOR_ORDER.length) % COLOR_ORDER.length;

            if (clockwise <= counterClockwise) {
                for (int j = 0; j < clockwise; j++) {
                    solutionClicks.add(new SolutionClick(ClickType.PICKUP_ALL, slot)); // left click
                }
            } else {
                for (int j = 0; j < counterClockwise; j++) {
                    solutionClicks.add(new SolutionClick(ClickType.PICKUP, slot)); // right click
                }
            }
        }

        this.solution = new Solution(solutionClicks);
        this.solveState = SolveState.SOLVED;
    }

    private <T> int indexOf(T[] array, T val) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == val) return i;
        }
        return -1;
    }

    private boolean isRubixPane(Item item) {
        return item == Items.BLUE_STAINED_GLASS_PANE
                || item == Items.RED_STAINED_GLASS_PANE
                || item == Items.ORANGE_STAINED_GLASS_PANE
                || item == Items.YELLOW_STAINED_GLASS_PANE
                || item == Items.GREEN_STAINED_GLASS_PANE;
    }

    protected static Rubix supply(ClientboundOpenScreenPacket packet) {
        return new Rubix(packet);
    }
}
