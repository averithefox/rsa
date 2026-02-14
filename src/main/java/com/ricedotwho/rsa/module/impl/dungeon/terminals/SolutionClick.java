package com.ricedotwho.rsa.module.impl.dungeon.terminals;

import net.minecraft.world.inventory.ClickType;

// Button 0 is middleclick and left click
// Buton 1 is right click
public record SolutionClick(ClickType type, int index, int button) {

}
