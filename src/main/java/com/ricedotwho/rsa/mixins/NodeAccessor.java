package com.ricedotwho.rsa.mixins;

import com.github.stivais.commodore.nodes.LiteralNode;
import com.github.stivais.commodore.nodes.Node;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Node.class)
public interface NodeAccessor {
  @Invoker("build$Commodore_modern")
  void build(LiteralNode parent);
}
