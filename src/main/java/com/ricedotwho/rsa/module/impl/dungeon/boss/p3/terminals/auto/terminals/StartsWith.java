package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals;

import com.ricedotwho.rsm.RSM;
import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import rsa.module.impl.dungeon.boss.p3.terminals.auto.AutoTerms;
import rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.Terminal;
import rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.TerminalType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartsWith extends Terminal {
  protected StartsWith(ClientboundOpenScreenPacket packet, AbstractContainerMenu menu) {
    super(TerminalType.STARTSWITH, packet, menu);
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
        hashInfo.setEnchanted(true);
      infos.add(hashInfo);
    }

    return getTerminalState(TerminalType.STARTSWITH, infos);
  }

  @Override
  public @NotNull TerminalState getCurrentState() {
    List<HashInfo> infos = new ArrayList<>(this.getType().getSlotCount());
    for (int i = 0; i < this.getType().getSlotCount(); i++) {
      Slot slot = this.getTerminalContainer().getSlot(i);
      infos.add(new HashInfo(slot.getItem()));
    }

    return getTerminalState(TerminalType.STARTSWITH, infos);
  }

  @Override
  public void solve() {
    super.solve();
    Pattern pattern = Pattern.compile("What starts with: '(\\w+)'?");
    Matcher matcher = pattern.matcher(this.getTitle());

    if (!matcher.find()) {
      return;
    }

    String matchLetter = matcher.group(1).toLowerCase();

    List<SolutionClick> solutionClicks = new ArrayList<>();

    for (Slot slot : this.getTerminalContainer().slots) {
      ItemStack stack = slot.getItem();

      if (stack.isEmpty()) continue;
      if (RSM.getModule(AutoTerms.class).getClickedSlotsTracker().contains(slot))
        continue; // Fuck you, isEnchanted check doesn;t work

      String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).toLowerCase();

      if (name.startsWith(matchLetter)) {
        solutionClicks.add(new SolutionClick(ClickType.CLONE, slot.index, 0));
      }
    }

    this.setSolution(new Solution(solutionClicks));
    this.setSolveState(SolveState.SOLVED);
  }

  @Override
  public boolean isEnabled() {
    return AutoTerms.getTerminals().get("Starts With");
  }

  public static StartsWith supply(ClientboundOpenScreenPacket packet, AbstractContainerMenu menu) {
    return new StartsWith(packet, menu);
  }
}
