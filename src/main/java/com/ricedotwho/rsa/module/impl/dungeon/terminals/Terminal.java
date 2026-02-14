package com.ricedotwho.rsa.module.impl.dungeon.terminals;

import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class Terminal {
    @Getter
    private final TerminalType type;

    @Getter
    protected SolveState solveState;

    @Getter
    private final String title;
    @Getter
    private final int windowID;

    protected final ItemStack[] items;

    @Getter
    Solution solution;


    protected Terminal(TerminalType type, ClientboundOpenScreenPacket packet) {
        this.type = type;
        this.windowID = packet.getContainerId();
        this.title = packet.getTitle().getString();
        this.solveState = SolveState.NOT_LOADED;
        this.items = new ItemStack[type.getSlotCount() + 1];
    }

    public void loadSlot(ClientboundContainerSetSlotPacket packet) {
        if (packet.getContainerId() != this.getWindowID()) {
            ChatUtils.chat("Window ID slot load mismatch!");
            return;
        }

        if (packet.getSlot() > this.type.getSlotCount()) {
            if (this.solveState == SolveState.NOT_LOADED)
                this.solveState = SolveState.LOADED;
            return;
        }

        items[packet.getSlot()] = packet.getItem();
    }

    public boolean shouldSolve() {
        return this.solveState != SolveState.NOT_LOADED;
    }

    public boolean isSolved() {
        return this.solution != null && this.solveState != SolveState.NOT_LOADED;
    }

    public void solve() {
        if (this.solveState == SolveState.NOT_LOADED) throw new IllegalStateException("Tried to solve incomplete terminal!");
    }


    public static Terminal fromPacket(ClientboundOpenScreenPacket packet) {
        MenuType<?> menuType = packet.getType();
        if (menuType != MenuType.GENERIC_9x4 && menuType != MenuType.GENERIC_9x5 && menuType != MenuType.GENERIC_9x6) return null;
        return findTerminalClass(packet);
    }


    private static Terminal findTerminalClass(ClientboundOpenScreenPacket packet) {
        TerminalType terminalType = TerminalType.getType(packet.getTitle().getString());
        if (terminalType == null) return null;
        return terminalType.supply(packet);
    }
}
