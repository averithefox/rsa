package com.ricedotwho.rsa.module.impl.dungeon.terminals;

import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class RedGreen extends Terminal {

    protected RedGreen(ClientboundOpenScreenPacket packet) {
        super(TerminalType.REDGREEN, packet);
    }

    @Override
    public void solve() {
        super.solve();

        List<SolutionClick> solutionClicks = new ArrayList<>();
        for (int slot = 0; slot < this.items.length; slot++) {
            ItemStack stack = this.items[slot];
            if (stack == null || stack.isEmpty()) continue;
            if (stack.getItem() != Items.RED_STAINED_GLASS_PANE) continue;

            solutionClicks.add(new SolutionClick(ClickType.PICKUP_ALL, slot));
        }

        this.solution = new Solution(solutionClicks);
        this.solveState = SolveState.SOLVED;
    }

    protected static RedGreen supply(ClientboundOpenScreenPacket packet) {
        return new RedGreen(packet);
    }
}
