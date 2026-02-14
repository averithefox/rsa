package com.ricedotwho.rsa.module.impl.dungeon.terminals;

import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartsWith extends Terminal {

    protected StartsWith(ClientboundOpenScreenPacket packet, AbstractContainerMenu menu) {
        super(TerminalType.STARTSWITH, packet, menu);
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

        List<SolutionClick> solutionClicks  = new ArrayList<>();

        for (Slot slot : this.terminalContainer.slots) {
            ItemStack stack = slot.getItem();

            if (stack.isEmpty()) continue;
            if (Boolean.TRUE.equals(stack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)) || stack.isEnchanted()) continue; // Fuck you, isEnchanted check doesn;t work

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).toLowerCase();

            if (name.startsWith(matchLetter)) {
                solutionClicks.add(new SolutionClick(ClickType.CLONE, slot.index, 0));
            }
        }

        this.solution = new Solution(solutionClicks);
        this.solveState = SolveState.SOLVED;
    }


    protected static StartsWith supply(ClientboundOpenScreenPacket packet, AbstractContainerMenu menu) {
        return new StartsWith(packet, menu);
    }
}
