package com.ricedotwho.rsa.module.impl.dungeon.terminals;

import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class Numbers extends Terminal {

    protected Numbers(ClientboundOpenScreenPacket packet) {
        super(TerminalType.NUMBERS, packet);
    }

    @Override
    public void solve() {
        super.solve();
        List<SolutionClick> sortedSlots = new ArrayList<>();
        IntStream.range(0, this.items.length).filter(i -> {
            ItemStack stack = this.items[i];
            return stack != null && !stack.isEmpty() && stack.getItem() == Items.RED_STAINED_GLASS_PANE;
        }).boxed()
                .sorted(Comparator.comparingInt(i -> this.items[i].getCount()))
                .map(i -> new SolutionClick(ClickType.PICKUP_ALL, i))
                .forEach(sortedSlots::add);

        this.solution = new Solution(sortedSlots);
        this.solveState = SolveState.SOLVED;
    }

    protected static Numbers supply(ClientboundOpenScreenPacket packet) {
        return new Numbers(packet);
    }
}
