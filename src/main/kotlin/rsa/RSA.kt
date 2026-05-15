package rsa

import com.ricedotwho.rsa.command.impl.AutoPetCommand
import com.ricedotwho.rsa.command.impl.BBGCommand
import com.ricedotwho.rsa.command.impl.BloodBlinkCommand
import com.ricedotwho.rsa.command.impl.DungeonBreakerCommand
import com.ricedotwho.rsa.command.impl.DynamicRouteCommand
import com.ricedotwho.rsa.command.impl.LavabounceCommand
import com.ricedotwho.rsa.command.impl.LimboCommand
import com.ricedotwho.rsa.command.impl.RSADevCommand
import com.ricedotwho.rsa.command.impl.RotateCommand
import com.ricedotwho.rsa.command.impl.RouteCommand
import com.ricedotwho.rsa.command.impl.SecretAuraCommand
import com.ricedotwho.rsa.command.impl.StopwatchCommand
import com.ricedotwho.rsa.command.impl.VelocityBufferCommand
import com.ricedotwho.rsa.component.impl.Edge
import com.ricedotwho.rsa.component.impl.Jump
import com.ricedotwho.rsa.component.impl.TickFreeze
import com.ricedotwho.rsa.component.impl.pathfinding.score.DungeonRoomScore
import com.ricedotwho.rsa.module.impl.dungeon.AutoUlt
import com.ricedotwho.rsa.module.impl.dungeon.BloodBlink
import com.ricedotwho.rsa.module.impl.dungeon.BloodCamp
import com.ricedotwho.rsa.module.impl.dungeon.DungeonBreaker
import com.ricedotwho.rsa.module.impl.dungeon.DynamicRoutes
import com.ricedotwho.rsa.module.impl.dungeon.FastLeap
import com.ricedotwho.rsa.module.impl.dungeon.InstantClear
import com.ricedotwho.rsa.module.impl.dungeon.SecretAura
import com.ricedotwho.rsa.module.impl.dungeon.SecretHitboxes
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AutoRoutes
import com.ricedotwho.rsa.module.impl.dungeon.boss.Blink
import com.ricedotwho.rsa.module.impl.dungeon.boss.BreakerAura
import com.ricedotwho.rsa.module.impl.dungeon.boss.p2.PadTimer
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.LavaBounce
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.TermAura
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.AutoP3
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.solver.TerminalSolver
import com.ricedotwho.rsa.module.impl.dungeon.boss.p4.InstaMid
import com.ricedotwho.rsa.module.impl.dungeon.boss.p5.Relics
import com.ricedotwho.rsa.module.impl.dungeon.croesus.AutoCroesus
import com.ricedotwho.rsa.module.impl.dungeon.device.AlignAura
import com.ricedotwho.rsa.module.impl.dungeon.device.Auto4
import com.ricedotwho.rsa.module.impl.dungeon.device.AutoSS
import com.ricedotwho.rsa.module.impl.dungeon.puzzle.Puzzles
import com.ricedotwho.rsa.module.impl.movement.VelocityBuffer
import com.ricedotwho.rsa.module.impl.other.AntiCheat
import com.ricedotwho.rsa.module.impl.other.AutoGfs
import com.ricedotwho.rsa.module.impl.other.AutoJax
import com.ricedotwho.rsa.module.impl.other.DevUtils
import com.ricedotwho.rsa.module.impl.other.FreezeState
import com.ricedotwho.rsa.module.impl.player.BonzoHelper
import com.ricedotwho.rsa.module.impl.player.CancelInteract
import com.ricedotwho.rsa.module.impl.player.autopet.AutoPet
import com.ricedotwho.rsa.module.impl.render.EffectsAndRender
import com.ricedotwho.rsa.module.impl.render.Esp
import com.ricedotwho.rsa.module.impl.render.Freecam
import com.ricedotwho.rsa.module.impl.render.HidePlayers
import com.ricedotwho.rsa.module.impl.render.PresetWaypoints
import com.ricedotwho.rsa.packet.sb.BloodClipHelperStartPacket
import com.ricedotwho.rsa.packet.sb.BloodClipHelperStopPacket
import com.ricedotwho.rsa.utils.render3d.type.Ring
import com.ricedotwho.rsm.addon.Addon
import com.ricedotwho.rsm.component.impl.Renderer3D
import com.ricedotwho.rsm.utils.ChatUtils
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import rsa.command.impl.AutoCroesusCommand
import rsa.module.impl.dungeon.boss.p3.terminals.auto.AutoTerms
import java.nio.file.Files
import java.nio.file.Path

object RSA : Addon {
  @JvmStatic
  @get:JvmName("SOUNDS_FOLDER")
  lateinit var SOUNDS_FOLDER: Path

  @JvmStatic
  val prefix: Component = Component.empty()
    .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
    .append(Component.literal("R").withColor(0xB263DF))
    .append(Component.literal("S").withColor(0xC57BEA))
    .append(Component.literal("A").withColor(0xD793F4))
    .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY))

  @JvmStatic
  val logger: Logger = LogManager.getLogger("rsa")

  override fun onInitialize() {
    // packet reg
    PayloadTypeRegistry.playC2S().register(BloodClipHelperStartPacket.TYPE, BloodClipHelperStartPacket.CODEC)
    PayloadTypeRegistry.playC2S().register(BloodClipHelperStopPacket.TYPE, BloodClipHelperStopPacket.CODEC)

    EffectsAndRender.init()

    Renderer3D.registerLine(Ring::class.java)

    SOUNDS_FOLDER = FabricLoader.getInstance()
      .configDir
      .resolve("rsm")
      .resolve("sounds")

    try {
      Files.createDirectories(SOUNDS_FOLDER)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun onUnload() = Unit

  override fun getModules() = listOf(
    AutoUlt(),
    BloodBlink(),
    BloodCamp(),
    DungeonBreaker(),
    DynamicRoutes(),
    FastLeap(),
    InstantClear(),
    SecretAura(),
    SecretHitboxes(),
    AutoRoutes(),
    Blink(),
    BreakerAura(),
    PadTimer(),
    LavaBounce(),
    TermAura(),
    AutoP3(),
    AutoTerms,
    TerminalSolver(),
    InstaMid(),
    Relics(),
    AutoCroesus(),
    AlignAura(),
    Auto4(),
    AutoSS(),
    Puzzles(),
    VelocityBuffer(),
    AntiCheat(),
    AutoGfs(),
    AutoJax(),
    DevUtils(),
    FreezeState(),
    BonzoHelper(),
    CancelInteract(),
    AutoPet(),
    EffectsAndRender(),
    Esp(),
    Freecam(),
    HidePlayers(),
    PresetWaypoints(),
  )

  override fun getComponents() = listOf(
    Edge(), Jump(), TickFreeze(), DungeonRoomScore()
  )

  override fun getCommands() = listOf(
    AutoCroesusCommand(),
    AutoPetCommand(),
    BBGCommand(),
    BloodBlinkCommand(),
    DungeonBreakerCommand(),
    DynamicRouteCommand(),
    LavabounceCommand(),
    LimboCommand(),
    RotateCommand(),
    RouteCommand(),
    RSADevCommand(),
    SecretAuraCommand(),
    StopwatchCommand(),
    VelocityBufferCommand()
  )

  @JvmStatic
  fun isInTestEnv(): Boolean {
    val mc = Minecraft.getInstance()
    val conn = mc.connection ?: return false
    return conn.connection.remoteAddress.toString().contains("hypixelp3sim.zapto.org") || mc.hasSingleplayerServer()
  }

  @JvmStatic
  fun chat(message: Any, vararg objects: Any) {
    ChatUtils.chatClean(prefix.copy().append(String.format(message.toString(), *objects)))
  }

  @JvmStatic
  fun chat(text: String) {
    ChatUtils.chatClean(prefix.copy().append(text))
  }

  @JvmStatic
  fun chat(component: Component) {
    ChatUtils.chatClean(prefix.copy().append(component))
  }
}
