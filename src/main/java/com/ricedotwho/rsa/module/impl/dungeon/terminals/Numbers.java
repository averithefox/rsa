package com.ricedotwho.rsa.module.impl.dungeon.terminals;

import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class Numbers extends Terminal {

    protected Numbers(ClientboundOpenScreenPacket packet, AbstractContainerMenu menu) {
        super(TerminalType.NUMBERS, packet, menu);
    }

    @Override
    public void solve() {
        super.solve();
        List<SolutionClick> sortedSlots = this.terminalContainer.slots.stream()
                .filter(slot -> slot.getItem().getItem() == Items.RED_STAINED_GLASS_PANE)
                        .sorted(Comparator.comparingInt(slot -> slot.getItem().getCount()))
                                .map(slot -> new SolutionClick(ClickType.CLONE, slot.index, 0))
                                        .toList();

        this.solution = new Solution(sortedSlots);
        this.solveState = SolveState.SOLVED;
    }

    protected static Numbers supply(ClientboundOpenScreenPacket packet, AbstractContainerMenu menu) {
        return new Numbers(packet, menu);
    }
}
