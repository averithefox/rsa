package com.ricedotwho.rsa.module.impl.dungeon.terminals;

import com.ricedotwho.rsa.module.impl.dungeon.AutoTerms;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Melody extends Terminal {

    private LinkedList<SolutionClick> queue;

    protected Melody(ClientboundOpenScreenPacket packet, AbstractContainerMenu menu) {
        super(TerminalType.MELODY, packet, menu);
        this.queue = new LinkedList<>();
    }

    @Override
    public TerminalState getNextState() {
        return this.getCurrentState();
    }

    @Override
    public TerminalState getCurrentState() {
        List<HashInfo> infos = new ArrayList<>(this.getType().getSlotCount());
        for (int i = 0; i < this.getType().getSlotCount(); i++) {
            Slot slot = this.terminalContainer.getSlot(i);
            infos.add(new HashInfo(slot.getItem()));
        }

        return Terminal.getTerminalState(TerminalType.MELODY, infos);
    }

    public boolean onTickStart(AutoTerms autoTerms) {
        if (queue.isEmpty()) return false;
        SolutionClick click = queue.removeFirst();
        autoTerms.sendWindowClick(click);
        return true;
    }

    @Override
    public void solve() {
        super.solve();

        this.solution = new Solution(Collections.emptyList());
        this.solveState = SolveState.SOLVED;
    }

    @Override
    public void loadSlot(ClientboundContainerSetSlotPacket packet) {
        super.loadSlot(packet);
        int slot = packet.getSlot();
        if (packet.getContainerId() != this.getWindowID()) return;
        if (slot < 10 || slot >= this.getType().getSlotCount()) return;
        ItemStack stack = packet.getItem();
        if (stack.getItem() != Items.LIME_STAINED_GLASS_PANE) return;
        if (this.terminalContainer.slots.get(slot % 9).getItem().getItem() != Items.MAGENTA_STAINED_GLASS_PANE) return;
        int buttonIndex = ((slot / 9) - 1) * 9 + 16;
        int mod = slot % 9;

        this.queue.clear();
        AutoTerms module = RSM.getModule(AutoTerms.class);
        boolean skip = module.getMelodySkip().getValue() && (mod == 1 || mod == 5) && (!module.getMelodySkipFirst().getValue() || buttonIndex > 18);
        if (!skip) {
            this.queue.add(new SolutionClick(ClickType.CLONE, buttonIndex, 0));
            return;
        }

        while (buttonIndex <= 43) {
            this.queue.add(new SolutionClick(ClickType.CLONE, buttonIndex, 0));
            buttonIndex += 9;
        }
    }




    protected static Melody supply(ClientboundOpenScreenPacket packet, AbstractContainerMenu menu) {
        return new Melody(packet, menu);
    }
}
