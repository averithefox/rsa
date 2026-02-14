package com.ricedotwho.rsa.module.impl.dungeon.terminals;

import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class RedGreen extends Terminal {

    protected RedGreen(ClientboundOpenScreenPacket packet, AbstractContainerMenu menu) {
        super(TerminalType.REDGREEN, packet, menu);
    }

    @Override
    public void solve() {
        super.solve();

        List<SolutionClick> solutionClicks = new ArrayList<>();
        for (Slot slot : this.terminalContainer.slots) {
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) continue;
            if (stack.getItem() != Items.RED_STAINED_GLASS_PANE) continue;

            solutionClicks.add(new SolutionClick(ClickType.CLONE, slot.index, 0));
        }

        this.solution = new Solution(solutionClicks);
        this.solveState = SolveState.SOLVED;
    }

    protected static RedGreen supply(ClientboundOpenScreenPacket packet, AbstractContainerMenu menu) {
        return new RedGreen(packet, menu);
    }
}
