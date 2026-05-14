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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Colors extends Terminal {
  protected Colors(ClientboundOpenScreenPacket packet, AbstractContainerMenu menu) {
    super(TerminalType.COLORS, packet, menu);
  }

  private static final Map<String, String> COLOR_REPLACEMENTS = Map.of(
    "light gray", "silver",
    "wool", "white",
    "bone", "white",
    "ink", "black",
    "lapis", "blue",
    "cocoa", "brown",
    "dandelion", "yellow",
    "rose", "red",
    "cactus", "green"
  );

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

    return getTerminalState(TerminalType.COLORS, infos);
  }

  @Override
  public @NotNull TerminalState getCurrentState() {
    List<HashInfo> infos = new ArrayList<>(this.getType().getSlotCount());
    for (int i = 0; i < this.getType().getSlotCount(); i++) {
      Slot slot = this.getTerminalContainer().getSlot(i);
      infos.add(new HashInfo(slot.getItem()));
    }

    return getTerminalState(TerminalType.COLORS, infos);
  }

  @Override
  public void solve() {
    super.solve();
    Pattern pattern = Pattern.compile("Select all the (.+) items!");
    Matcher matcher = pattern.matcher(this.getTitle());

    if (!matcher.find()) {
      return;
    }

    String color = matcher.group(1).toLowerCase();

    List<SolutionClick> solutionClicks = new ArrayList<>();

    for (Slot slot : this.getTerminalContainer().slots) {
      ItemStack stack = slot.getItem();

      if (stack.isEmpty()) continue;
      if (RSM.getModule(AutoTerms.class).getClickedSlotsTracker().contains(slot))
        continue; // Fuck you, isEnchanted check doesn;t work

      String fixedName = fixColorItemName(ChatFormatting.stripFormatting(stack.getHoverName().getString()).toLowerCase());

      if (fixedName.startsWith(color)) {
        solutionClicks.add(new SolutionClick(ClickType.CLONE, slot.index, 0));
      }
    }

    this.setSolution(new Solution(solutionClicks));
    this.setSolveState(SolveState.SOLVED);
  }

  @Override
  public boolean isEnabled() {
    return AutoTerms.getTerminals().get("Colours");
  }

  private String fixColorItemName(String itemName) {
    for (Map.Entry<String, String> entry : COLOR_REPLACEMENTS.entrySet()) {
      String from = entry.getKey();
      String to = entry.getValue();

      if (itemName.startsWith(from)) {
        itemName = to + itemName.substring(from.length());
      }
    }
    return itemName;
  }

  public static Colors supply(ClientboundOpenScreenPacket packet, AbstractContainerMenu menu) {
    return new Colors(packet, menu);
  }
}
