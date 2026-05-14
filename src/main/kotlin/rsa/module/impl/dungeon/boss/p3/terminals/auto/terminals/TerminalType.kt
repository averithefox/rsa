package rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals

import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.Colors
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.Melody
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.Numbers
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.RedGreen
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.Rubix
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.StartsWith
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.world.inventory.AbstractContainerMenu

enum class TerminalType(
  val id: Int,
  val title: String,
  val slotCount: Int,
  val supply: (packet: ClientboundOpenScreenPacket, menu: AbstractContainerMenu) -> Terminal
) {
  NUMBERS(0, "Click in order!", 35, Numbers::supply),
  COLORS(1, "Select all the", 53, Colors::supply),
  STARTSWITH(2, "What starts with:", 44, StartsWith::supply),
  RUBIX(3, "Change all to same color!", 44, Rubix::supply),
  REDGREEN(4, "Correct all the panes!", 44, RedGreen::supply),
  MELODY(5, "Click the button on time!", 44, Melody::supply);

  companion object {
    @JvmStatic
    fun getType(s: String) = entries.find { s.startsWith(it.title) }
  }
}
