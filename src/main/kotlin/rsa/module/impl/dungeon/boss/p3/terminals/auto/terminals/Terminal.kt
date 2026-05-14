package rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals

import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.Solution
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.SolveState
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.TerminalState
import net.minecraft.core.component.DataComponents
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import rsa.RSA

abstract class Terminal(
  val type: TerminalType,
  packet: ClientboundOpenScreenPacket,
  protected val terminalContainer: AbstractContainerMenu
) {
  protected val title: String = packet.title.string
  val windowId = packet.containerId

  protected var solveState = SolveState.NOT_LOADED
  var solution: Solution? = null
    protected set

  val shouldSolve get() = solveState != SolveState.NOT_LOADED
  val isSolved get() = shouldSolve && solution != null

  abstract val isEnabled: Boolean

  abstract fun getNextState(): TerminalState
  abstract fun getCurrentState(): TerminalState

  open fun loadSlot(packet: ClientboundContainerSetSlotPacket) {
    if (packet.containerId != windowId) {
      RSA.chat("Window ID slot load mismatch! -> term: $windowId, packet: ${packet.containerId}")
      return
    }

    if (packet.slot == type.slotCount - 1 && this.solveState == SolveState.NOT_LOADED) {
      this.solveState = SolveState.LOADED
      return
    }
  }

  open fun solve() {
    if (solveState == SolveState.NOT_LOADED)
      throw IllegalStateException("Tried to solve incomplete terminal!")
  }

  protected fun getTerminalState(type: TerminalType, stacks: MutableList<HashInfo>): TerminalState {
    var hash = 1
    for (stack in stacks) {
      hash = 31 * hash + stack.itemHash
      hash = 31 * hash + stack.stackSize
      hash = 31 * hash + if (stack.isEnchanted) 1 else 0
    }

    return TerminalState(type, hash)
  }

  companion object {
    @JvmStatic
    fun fromPacket(packet: ClientboundOpenScreenPacket, menu: AbstractContainerMenu): Terminal? {
      if (packet.type !== MenuType.GENERIC_9x4 && packet.type !== MenuType.GENERIC_9x5 && packet.type !== MenuType.GENERIC_9x6) return null
      return findTerminalClass(packet, menu)
    }

    @JvmStatic
    private fun findTerminalClass(packet: ClientboundOpenScreenPacket, menu: AbstractContainerMenu): Terminal? {
      val type = TerminalType.getType(packet.title.string)
      return type?.run { supply(packet, menu) }
    }
  }

  protected data class HashInfo(var isEnchanted: Boolean, var itemHash: Int, var stackSize: Int) {
    constructor(stack: ItemStack) : this(
      stack.isEnchantable || (stack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE) == true),
      stack.hashCode(),
      stack.count
    )
  }
}
