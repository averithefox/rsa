package rsa

import com.ricedotwho.rsa.command.impl.*
import com.ricedotwho.rsa.component.impl.Edge
import com.ricedotwho.rsa.component.impl.Jump
import com.ricedotwho.rsa.component.impl.TickFreeze
import com.ricedotwho.rsa.component.impl.pathfinding.score.DungeonRoomScore
import com.ricedotwho.rsa.module.impl.dungeon.*
import com.ricedotwho.rsa.module.impl.dungeon.autoroutes.AutoRoutes
import com.ricedotwho.rsa.module.impl.dungeon.boss.Blink
import com.ricedotwho.rsa.module.impl.dungeon.boss.BreakerAura
import com.ricedotwho.rsa.module.impl.dungeon.boss.p2.PadTimer
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.LavaBounce
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.TermAura
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.AutoP3
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.auto.AutoTerms
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.solver.TerminalSolver
import com.ricedotwho.rsa.module.impl.dungeon.boss.p4.InstaMid
import com.ricedotwho.rsa.module.impl.dungeon.boss.p5.Relics
import com.ricedotwho.rsa.module.impl.dungeon.croesus.AutoCroesus
import com.ricedotwho.rsa.module.impl.dungeon.device.AlignAura
import com.ricedotwho.rsa.module.impl.dungeon.device.Auto4
import com.ricedotwho.rsa.module.impl.dungeon.device.AutoSS
import com.ricedotwho.rsa.module.impl.dungeon.puzzle.Puzzles
import com.ricedotwho.rsa.module.impl.movement.VelocityBuffer
import com.ricedotwho.rsa.module.impl.other.*
import com.ricedotwho.rsa.module.impl.player.BonzoHelper
import com.ricedotwho.rsa.module.impl.player.CancelInteract
import com.ricedotwho.rsa.module.impl.player.autopet.AutoPet
import com.ricedotwho.rsa.module.impl.render.*
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
    AutoUlt::class,
    BloodBlink::class,
    BloodCamp::class,
    DungeonBreaker::class,
    DynamicRoutes::class,
    FastLeap::class,
    InstantClear::class,
    SecretAura::class,
    SecretHitboxes::class,
    AutoRoutes::class,
    Blink::class,
    BreakerAura::class,
    PadTimer::class,
    LavaBounce::class,
    TermAura::class,
    AutoP3::class,
    AutoTerms::class,
    TerminalSolver::class,
    InstaMid::class,
    Relics::class,
    AutoCroesus::class,
    AlignAura::class,
    Auto4::class,
    AutoSS::class,
    Puzzles::class,
    VelocityBuffer::class,
    AntiCheat::class,
    AutoGfs::class,
    AutoJax::class,
    DevUtils::class,
    FreezeState::class,
    BonzoHelper::class,
    CancelInteract::class,
    AutoPet::class,
    EffectsAndRender::class,
    Esp::class,
    Freecam::class,
    HidePlayers::class,
    PresetWaypoints::class,
  ).map { it.java }

  override fun getComponents() = listOf(
    Edge::class,
    Jump::class,
    TickFreeze::class,
    DungeonRoomScore::class
  ).map { it.java }

  override fun getCommands() = listOf(
    AutoCroesusCommand::class,
    AutoPetCommand::class,
    BBGCommand::class,
    BloodBlinkCommand::class,
    DungeonBreakerCommand::class,
    DynamicRouteCommand::class,
    LavabounceCommand::class,
    LimboCommand::class,
    RotateCommand::class,
    RouteCommand::class,
    RSADevCommand::class,
    SecretAuraCommand::class,
    StopwatchCommand::class,
    VelocityBufferCommand::class
  ).map { it.java }

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
