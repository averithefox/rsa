package com.ricedotwho.rsa.mixins;

import com.github.stivais.commodore.Commodore;
import com.github.stivais.commodore.nodes.LiteralNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Commodore.class)
public interface CommodoreAccessor {
  @Accessor("root")
  LiteralNode getRoot();
}
