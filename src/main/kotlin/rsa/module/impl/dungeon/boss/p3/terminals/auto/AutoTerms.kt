package rsa.module.impl.dungeon.boss.p3.terminals.auto

import com.ricedotwho.rsa.event.impl.RawTickEvent
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.ClickedSlotsTracker
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.Colors
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.Melody
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.SolutionClick
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.StartsWith
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.TerminalState
import com.ricedotwho.rsm.RSM
import com.ricedotwho.rsm.component.impl.location.Island
import com.ricedotwho.rsm.component.impl.location.Location
import com.ricedotwho.rsm.event.api.EventPriority
import com.ricedotwho.rsm.event.api.SubscribeEvent
import com.ricedotwho.rsm.event.impl.client.InputPollEvent
import com.ricedotwho.rsm.event.impl.client.PacketEvent
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent
import com.ricedotwho.rsm.event.impl.render.Render3DEvent
import com.ricedotwho.rsm.event.impl.world.WorldEvent
import com.ricedotwho.rsm.module.Module
import com.ricedotwho.rsm.module.api.Category
import com.ricedotwho.rsm.module.api.ModuleInfo
import com.ricedotwho.rsm.ui.clickgui.settings.group.GroupSetting
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting
import com.ricedotwho.rsm.ui.clickgui.settings.impl.MultiBoolSetting
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting
import com.ricedotwho.rsm.utils.DungeonUtils
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.HashedStack
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.common.ClientboundPingPacket
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.world.entity.player.Input
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import rsa.RSA
import rsa.module.getValue
import rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.Terminal
import rsa.module.provideDelegate

@ModuleInfo(aliases = ["AutoTerms"], id = "AutoTerms", category = Category.DUNGEONS)
object AutoTerms : Module() {
  private var lastClickTime = 0L
  private var clickedWindow = false
  private var firstClick = true
  var terminal: Terminal? = null
    private set

  private var lastPingTicks = 100

  var terminalContainer: AbstractContainerMenu? = null
    private set
  val clickedSlotsTracker = ClickedSlotsTracker()

  private var predictedState: TerminalState? = null

  private val firstClickDelay by NumberSetting("First Click Delay", 200.0, 600.0, 400.0, 5.0)
  private val delay by NumberSetting("Delay", 100.0, 250.0, 150.0, 5.0)
  private val breakThreshold by NumberSetting("Break Threshold", 200.0, 800.0, 500.0, 10.0)

  @JvmStatic
  val terminals: Map<String, Boolean> by MultiBoolSetting(
    "Terminals",
    listOf("Colours", "Melody", "Numbers", "Red Green", "Rubix", "Starts With"),
    listOf("Colours", "Melody", "Numbers", "Red Green", "Rubix", "Starts With")
  )

  val melodySkip: Boolean by BooleanSetting("Melody Skip", true)
  val melodySkipFirst: Boolean by BooleanSetting("Don't Skip First", true)
  val announceMelody: Boolean by BooleanSetting("Announce Melody", true)

  private val noLimbo by BooleanSetting("No Limbo", true)
  private val invWalkGroup by GroupSetting("Invwalk", InvWalk)

  @SubscribeEvent
  private fun onWorldLoad(event: WorldEvent.Load) {
    close()
    lastPingTicks = 100
  }

  @SubscribeEvent
  private fun render(event: Render3DEvent.Last) {
    val terminal = terminal ?: return
    if (!isInTerm() || terminal is Melody) return

    if (terminal.shouldSolve && !terminal.isSolved) {
      terminal.solve()
    }

    if (!terminal.isSolved) return

    predictedState?.let {
      val newState = terminal.getCurrentState()
      if (!it.matches(newState)) {
        firstClick = true
        lastClickTime = System.currentTimeMillis()
        clickedSlotsTracker.clear()
      }
      predictedState = null
    }

    if (!terminal.isEnabled) return

    // todo: DO NOT DO THIS ON RENDER!

    val lastClick = System.currentTimeMillis() - lastClickTime
    val delay = if (firstClick) firstClickDelay else delay
    if (lastClick < delay.toDouble()) return
    if (lastClick > breakThreshold.toDouble()) {
      clickedWindow = true
    }

    // Why is there another check here?
    if (!isInTerm() || clickedWindow) return

    if (!terminal.isSolved) return
    val solution = terminal.solution ?: return
    if (solution.length < 1) return

    sendWindowClick(solution.next)
    lastClickTime = System.currentTimeMillis()
    clickedWindow = true
    firstClick = false
  }

  @SubscribeEvent
  private fun onPollInput(event: InputPollEvent) {
    if (invWalkGroup.melodyMoveCounter < 1) return
    if (mc.screen == null && !isInTerm()) {
      invWalkGroup.melodyMoveCounter = 0
      return
    }

    val oldInputs = event.clientInput
    val newInputs = Input(false, false, false, false, false, oldInputs.shift, false)
    event.input.apply(newInputs)

    --invWalkGroup.melodyMoveCounter
  }

  @SubscribeEvent
  private fun onTick(event: ClientTickEvent.Start) {
    --lastPingTicks
    if (!isInTerm()) {
      firstClick = true
      clickedSlotsTracker.clear()
      lastClickTime = System.currentTimeMillis()
    }
  }

  @SubscribeEvent
  private fun onRawTick(event: RawTickEvent) {
    val terminal = terminal ?: return
    if (!isInTerm() || terminal !is Melody || !terminal.isEnabled) return
    if (terminal.onTickStart(this)) {
      invWalkGroup.onMelodyClick()
    }
  }

  @SubscribeEvent
  private fun onPlayerTick(event: ClientTickEvent.Player) {
    val player = mc.player ?: return
    if (Location.getArea() != Island.Dungeon || !DungeonUtils.isPositionInF7Boss(player.position()) || !isInTerm() || !invWalkGroup.isEnabled) return
    if (lastPingTicks < 0) event.isCancelled = true
  }

  // If a gui is open request is sent at the same time as term aura sends a click packet while not in term,
  // if the original gui opens first, the term gui will open after the client has opened it

  /// This should run before {@link Terminals#onPacket(PacketEvent.Receive)}
  @SubscribeEvent(priority = EventPriority.HIGH)
  private fun onReceivePacket(event: PacketEvent.Receive) {
    val cancel = !invWalkGroup.invwalkMaybeFix
    when (val packet = event.packet) {
      is ClientboundPingPacket -> {
        lastPingTicks = 5
      }

      is ClientboundOpenScreenPacket -> {
        if (packet.containerId !in 1..100) return
        val player = mc.player ?: return

        val predState = terminal?.run { if (isSolved) getNextState() else null } ?: TerminalState(null, 0)

        val terminalContainer = packet.type.create(packet.containerId, player.inventory)
        this.terminalContainer = terminalContainer
        terminal = Terminal.fromPacket(packet, terminalContainer)
        if (terminal == null) {
          this.terminalContainer = null
          return
        }

        // should run after?
        val screen = mc.screen
        if (screen is AbstractContainerScreen<*> && screen.menu.containerId != 0) {
          // o7 Balding
          mc.setScreen(null)
        }

        predictedState = predState
        clickedWindow = false
        invWalkGroup.terminalRenderer.newWindow(terminalContainer)

        if (invWalkGroup.isEnabled) {
          event.isCancelled = true
          if (invWalkGroup.invwalkMaybeFix) {
            setContainerMenu(packet.type, packet.containerId, packet.title)
          }
        }
      }

      is ClientboundContainerSetSlotPacket -> {
        val (terminal, terminalContainer) = (terminal ?: return) to (terminalContainer ?: return)
        if (packet.containerId == 0 || packet.containerId != terminalContainer.containerId) return
        terminalContainer.setItem(packet.slot, packet.stateId, packet.item)
        terminal.loadSlot(packet)
        if (invWalkGroup.isEnabled && cancel) event.isCancelled = true
      }

      is ClientboundContainerClosePacket -> {
        val terminalContainer = terminalContainer ?: return
        if (packet.containerId != terminalContainer.containerId) {
          RSA.chat("Container ID mismatch on close!")
          close()
          return
        }

        close()
        if (invWalkGroup.isEnabled) event.isCancelled = true
      }

      is ClientboundSetCursorItemPacket -> {
        if (!isInTerm()) return
        if (invWalkGroup.isEnabled && cancel) event.isCancelled = true
      }

      is ClientboundContainerSetContentPacket -> {
        if (!isInTerm()) return
        if (packet.containerId != 0 && invWalkGroup.isEnabled && cancel) event.isCancelled = true
      }

      is ClientboundHorseScreenOpenPacket -> {
        if (!isInTerm()) return
        reset()
      }

      is ClientboundContainerSetDataPacket -> {
        if (!isInTerm()) return
        if (packet.containerId != 0 && invWalkGroup.isEnabled && cancel) event.isCancelled = true
      }

      is ClientboundMerchantOffersPacket -> {
        if (!isInTerm()) return
        reset()
      }
    }
  }

  private fun <T : AbstractContainerMenu> setContainerMenu(menuType: MenuType<T>, i: Int, component: Component) {
    val screenConstructor = MenuScreens.getConstructor(menuType) ?: return RSA.logger.warn(
      "Failed to create screen for menu type: {}", BuiltInRegistries.MENU.getKey(menuType)
    )
    val inventory = mc.player?.inventory ?: return
    val screen = screenConstructor.create(menuType.create(i, inventory), inventory, component)
    mc.player?.containerMenu = screen.menu
  }

  private fun sendWindowClick(
    windowId: Int, click: SolutionClick, player: Player, containerMenu: AbstractContainerMenu
  ) {
    if (windowId != containerMenu.containerId) {
      RSA.chat("Window ID mismatch!")
      return
    }

    val conn = mc.connection ?: return
    val nonNullList = containerMenu.slots
    val list = ArrayList<ItemStack>(nonNullList.size)
    nonNullList.forEach { slot ->
      list.add(slot.item.copy())
    }

    containerMenu.clicked(click.index(), click.button(), click.type(), player)

    val int2ObjectMap = Int2ObjectOpenHashMap<HashedStack>()
    nonNullList.forEachIndexed { index, slot ->
      val itemStack = list[index]
      val itemStack2 = slot.item
      if (!ItemStack.matches(itemStack, itemStack2)) {
        int2ObjectMap[index] = HashedStack.create(itemStack2, conn.decoratedHashOpsGenenerator())
      }
    }

    val hashedStack = HashedStack.create(containerMenu.carried, conn.decoratedHashOpsGenenerator())
    conn.send(
      ServerboundContainerClickPacket(
        windowId,
        containerMenu.stateId,
        click.index().toShort(),
        click.button().toByte(),
        click.type(),
        int2ObjectMap,
        hashedStack
      )
    )
  }

  fun sendWindowClick(click: SolutionClick) {
    val terminal = terminal ?: return
    val terminalContainer = terminalContainer ?: return
    val player = mc.player ?: return
    if (click.index() < 0 || click.index() >= terminal.type.slotCount) return
    if (terminal is StartsWith || terminal is Colors) {
      clickedSlotsTracker.clickSlot(terminalContainer.getSlot(click.index()))
    }
    sendWindowClick(terminal.windowId, click, player, terminalContainer)
  }

  private fun close() {
    terminal = null
    invWalkGroup.terminalRenderer.close()
    terminalContainer = null
    predictedState = null
    firstClick = true
    lastClickTime = System.currentTimeMillis()
    clickedSlotsTracker.clear()
  }

  @JvmStatic
  fun isInTerminal() = RSM.getModule(this.javaClass).isInTerm()

  fun isInTerm() = terminal != null && terminalContainer != null
}
