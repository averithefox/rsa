package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals;

import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import rsa.module.impl.dungeon.boss.p3.terminals.auto.AutoTerms;
import rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.Terminal;
import rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.TerminalType;

import java.util.ArrayList;
import java.util.List;

public class RedGreen extends Terminal {
  protected RedGreen(ClientboundOpenScreenPacket packet, AbstractContainerMenu menu) {
    super(TerminalType.REDGREEN, packet, menu);
  }

  @Override
  public @NotNull TerminalState getNextState() {
    if (this.getSolution() == null) throw new IllegalStateException("Tried to get next state without solving!");

    List<HashInfo> infos = new ArrayList<>(this.getType().getSlotCount());
    int changedIndex = this.getSolution().getNext().index();
    for (int i = 0; i < this.getType().getSlotCount(); i++) {
      Slot slot = this.getTerminalContainer().getSlot(i);
      HashInfo hashInfo = new HashInfo(slot.getItem());
      if (slot.index == changedIndex)
        hashInfo.setItemHash(Items.LIME_STAINED_GLASS_PANE.hashCode());
      infos.add(hashInfo);
    }

    return getTerminalState(TerminalType.REDGREEN, infos);
  }

  @Override
  public @NotNull TerminalState getCurrentState() {
    List<HashInfo> infos = new ArrayList<>(this.getType().getSlotCount());
    for (int i = 0; i < this.getType().getSlotCount(); i++) {
      Slot slot = this.getTerminalContainer().getSlot(i);
      infos.add(new HashInfo(slot.getItem()));
    }

    return getTerminalState(TerminalType.REDGREEN, infos);
  }

  @Override
  public void solve() {
    super.solve();

    List<SolutionClick> solutionClicks = new ArrayList<>();
    for (Slot slot : this.getTerminalContainer().slots) {
      ItemStack stack = slot.getItem();
      if (stack.isEmpty()) continue;
      if (stack.getItem() != Items.RED_STAINED_GLASS_PANE) continue;

      solutionClicks.add(new SolutionClick(ClickType.CLONE, slot.index, 0));
    }

    this.setSolution(new Solution(solutionClicks));
    this.setSolveState(SolveState.SOLVED);
  }

  @Override
  public boolean isEnabled() {
    return AutoTerms.getTerminals().get("Red Green");
  }

  public static RedGreen supply(ClientboundOpenScreenPacket packet, AbstractContainerMenu menu) {
    return new RedGreen(packet, menu);
  }
}
