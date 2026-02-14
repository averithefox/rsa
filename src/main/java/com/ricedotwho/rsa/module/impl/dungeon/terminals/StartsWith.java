package com.ricedotwho.rsa.module.impl.dungeon.terminals;

import com.ricedotwho.rsm.utils.ChatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartsWith extends Terminal {

    protected StartsWith(ClientboundOpenScreenPacket packet) {
        super(TerminalType.STARTSWITH, packet);
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

        for (int slot = 0; slot < items.length; slot++) {
            ItemStack stack = items[slot];

            if (stack == null || stack.isEmpty()) continue;
            if (Boolean.TRUE.equals(stack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)) || stack.isEnchanted()) continue; // Fuck you, isEnchanted check doesn;t work

            String name = ChatFormatting.stripFormatting(stack.getHoverName().getString()).toLowerCase();

            if (name.startsWith(matchLetter)) {
                if (solutionClicks.isEmpty()) ChatUtils.chat(stack);
                solutionClicks.add(new SolutionClick(ClickType.PICKUP_ALL, slot));
            }
        }

        this.solution = new Solution(solutionClicks);
        this.solveState = SolveState.SOLVED;
    }


    protected static StartsWith supply(ClientboundOpenScreenPacket packet) {
        return new StartsWith(packet);
    }
}
