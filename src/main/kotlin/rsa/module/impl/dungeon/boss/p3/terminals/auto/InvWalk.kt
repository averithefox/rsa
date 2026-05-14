package rsa.module.impl.dungeon.boss.p3.terminals.auto

import com.ricedotwho.rsa.component.impl.TickFreeze
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.terminals.TerminalRenderer
import com.ricedotwho.rsm.component.impl.Terminals
import com.ricedotwho.rsm.data.Colour
import com.ricedotwho.rsm.event.api.SubscribeEvent
import com.ricedotwho.rsm.event.impl.client.InputPollEvent
import com.ricedotwho.rsm.event.impl.render.Render2DEvent
import com.ricedotwho.rsm.module.SubModule
import com.ricedotwho.rsm.module.api.SubModuleInfo
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types.Melody
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ModeSetting
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting
import com.ricedotwho.rsm.utils.Utils
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils
import net.minecraft.world.entity.player.Input
import org.joml.Vector2d
import rsa.module.getValue
import rsa.module.provideDelegate
import kotlin.math.floor

@SubModuleInfo(name = "InvWalk", alwaysDisabled = false)
object InvWalk : SubModule<AutoTerms>(AutoTerms) {
  private val style by ModeSetting("Style", "Items", listOf("Solver", "Items"))
  @JvmStatic
  val useOverrides: Boolean by BooleanSetting("Use Overrides", true)
  private val renderTitles by BooleanSetting("Use Overrides", true)
  private val titleMCFont by BooleanSetting("Title MC Font", true)
  private val renderClicksLeft by BooleanSetting("Render clicks left", true)
  private val clicksMCFont by BooleanSetting("Clicks MC Font", true)
  private val titleColor by ColourSetting("Title Colour", Colour(96, 31, 158))
  private val remainingColor by ColourSetting("Remaining Colour", Colour(96, 31, 158))
  private val clicksColor by ColourSetting("Clicks Colour", Colour(0, 191, 0))
  private val textShadow by BooleanSetting("Text Shadow", false)

  private val moveDelayMode by ModeSetting("Mode Delay", "Freeze", listOf("Stop Inputs", "Freeze"))
  private val melodyMoveDelay by NumberSetting("Melody Move Delay", 0.0, 500.0, 300.0, 50.0)
  val invwalkMaybeFix: Boolean by BooleanSetting("Invwalk maybe fix", false)

  private val termTitle by DragSetting("Term Title", Vector2d(10.0, 10.0), Vector2d(150.0, 15.0))
  private val clicksText by DragSetting("Clicks Text", Vector2d(10.0, 10.0), Vector2d(150.0, 15.0))
  private val gui by DragSetting("Visualiser Gui", Vector2d(551.0, 330.0), Vector2d(144.0, 80.0))

  val terminalRenderer = TerminalRenderer()
  var melodyMoveCounter = 0
  private var lastMelodyClick = 0L

  override fun reset() {
    melodyMoveCounter = 0
  }

  @SubscribeEvent
  private fun onRenderGui(event: Render2DEvent) {
    if (!module.isInTerm()) return
    val slots = Utils.getGuiSlotCount(module.terminalContainer?.type)
    val currentTerm = Terminals.getCurrent()

    if (renderClicksLeft && currentTerm != null) {
      val remainingText = "Clicks remaining: "
      val clicks = if (currentTerm is Melody) "${currentTerm.progress}/4" else "${currentTerm.solution.size}"
      if (clicksMCFont) {
        clicksText.renderScaledGFX(event.gfx, {
          event.gfx.drawString(mc.font, remainingText, 0, 0, remainingColor.rgb)
          event.gfx.drawString(mc.font, clicks, mc.font.width(remainingText), 0, clicksColor.rgb)
        }, 150f, 15f)
      } else {
        clicksText.renderScaled(event.gfx, {
          val fontSize = 14f
          NVGUtils.drawText(remainingText, 0f, 0f, fontSize, remainingColor, textShadow, NVGUtils.PRODUCT_SANS)
          val remainingTextWidth = NVGUtils.getTextWidth(remainingText, fontSize, NVGUtils.PRODUCT_SANS)
          NVGUtils.drawText(clicks, remainingTextWidth, 0f, fontSize, clicksColor, textShadow, NVGUtils.PRODUCT_SANS)
        }, 150f, 15f)
      }
    }

    if (renderTitles && currentTerm != null) {
      var termText = "In ${Utils.capitalise(currentTerm.type.name.replace("_", "").lowercase())}"
      if (currentTerm is Melody) {
        val moveDelay = melodyMoveDelay.toInt()
        val now = System.currentTimeMillis()
        if (lastMelodyClick + moveDelay > now) termText += " ${lastMelodyClick - now + moveDelay}ms"
      }
      if (titleMCFont) {
        termTitle.renderScaledGFX(event.gfx, {
          event.gfx.drawString(mc.font, termText, 0, 0, titleColor.rgb)
        }, 150f, 15f)
      } else {
        termTitle.renderScaled(event.gfx, {
          NVGUtils.drawText(termText, 0f, 0f, 14f, titleColor, textShadow, NVGUtils.PRODUCT_SANS)
        }, 150f, 15f)
      }
    }

    if (style == "Items") {
      val width = 9f * 16f
      val height = floor(slots / 9f) * 16f
      gui.renderScaledGFX(
        event.gfx,
        { terminalRenderer.renderItems(event.gfx, module.terminal) },
        width,
        height
      )
    } else {
      val gap = 32 + TerminalSolver.getGap().value.toFloat()
      gui.renderScaled(event.gfx, { terminalRenderer.renderSolver(gap) }, 9f * gap, slots / 9f * gap)
    }
  }

  // This works for strafe but not for forwards and backwards for some reason
  @SubscribeEvent
  private fun onPollInput(event: InputPollEvent) {
    if (melodyMoveCounter < 1) return

    if (mc.screen == null && !module.isInTerm()) {
      melodyMoveCounter = 0
      return
    }

    val oldInputs = event.clientInput
    val newInputs = Input(false, false, false, false, false, oldInputs.shift, false)
    event.input.apply(newInputs)

    --melodyMoveCounter
  }

  fun onMelodyClick() {
    lastMelodyClick = System.currentTimeMillis()
    if (moveDelayMode == "Freeze") {
      TickFreeze.freeze(melodyMoveDelay.toLong(), true)
    } else {
      melodyMoveCounter = melodyMoveDelay.toInt() / 50
    }
  }
}
