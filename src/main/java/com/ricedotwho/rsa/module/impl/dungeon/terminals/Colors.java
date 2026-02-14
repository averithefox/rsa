package com.ricedotwho.rsa.module.impl.dungeon.terminals;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Colors extends Terminal {

    protected Colors(ClientboundOpenScreenPacket packet) {
        super(TerminalType.COLORS, packet);
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
    public void solve() {
        super.solve();
        Pattern pattern = Pattern.compile("Select all the (.+) items!");
        Matcher matcher = pattern.matcher(this.getTitle());

        if (!matcher.find()) {
            return;
        }

        String color = matcher.group(1).toLowerCase();

        List<SolutionClick> solutionClicks = new ArrayList<>();

        for (int slot = 0; slot < this.items.length; slot++) {
            ItemStack stack = this.items[slot];

            if (stack == null || stack.isEmpty()) continue;
            if (Boolean.TRUE.equals(stack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)) || stack.isEnchanted()) continue;

            String fixedName = fixColorItemName(ChatFormatting.stripFormatting(stack.getHoverName().getString()).toLowerCase());

            if (fixedName.startsWith(color)) {
                solutionClicks.add(new SolutionClick(ClickType.PICKUP_ALL, slot));
            }
        }

        this.solution = new Solution(solutionClicks);
        this.solveState = SolveState.SOLVED;
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



    protected static Colors supply(ClientboundOpenScreenPacket packet) {
        return new Colors(packet);
    }
}
