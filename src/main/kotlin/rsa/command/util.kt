package rsa.command

import com.github.stivais.commodore.Commodore
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.ricedotwho.rsa.mixins.CommodoreAccessor
import com.ricedotwho.rsa.mixins.NodeAccessor
import net.minecraft.client.multiplayer.ClientSuggestionProvider

fun Commodore.toLiteralArgumentBuilder(): LiteralArgumentBuilder<ClientSuggestionProvider> {
  this as CommodoreAccessor
  for (node in root.children) {
    node as NodeAccessor
    node.build(root)
  }
  @Suppress("unchecked_cast")
  return root.builder as LiteralArgumentBuilder<ClientSuggestionProvider>
}
