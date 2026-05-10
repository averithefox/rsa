package rsa.features.dungeon.croesus

import com.ricedotwho.rsa.module.impl.dungeon.croesus.AutoCroesus
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import rsa.RSA
import java.nio.file.Files

object CroesusLoader {
  private val worthlessFile = FileUtils.getSaveFileInCategory("croesus", "worthless.json")
  private val alwaysBuyFile = FileUtils.getSaveFileInCategory("croesus", "always_buy.json")
  private val runLogFile = FileUtils.getSaveFileInCategory("croesus", "run_log.json")

  @JvmStatic
  var worthless = mutableListOf(
    "DUNGEON_DISC_5",
    "DUNGEON_DISC_4",
    "DUNGEON_DISC_3",
    "DUNGEON_DISC_2",
    "DUNGEON_DISC_1",
    "MAXOR_THE_FISH",
    "STORM_THE_FISH",
    "GOLDOR_THE_FISH",
    "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_1",
    "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_2",
    "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_3",
    "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_4",
    "ENCHANTMENT_ULTIMATE_NO_PAIN_NO_GAIN_5",
    "ENCHANTMENT_ULTIMATE_COMBO_1",
    "ENCHANTMENT_ULTIMATE_COMBO_2",
    "ENCHANTMENT_ULTIMATE_COMBO_3",
    "ENCHANTMENT_ULTIMATE_COMBO_4",
    "ENCHANTMENT_ULTIMATE_COMBO_5",
    "ENCHANTMENT_ULTIMATE_BANK_1",
    "ENCHANTMENT_ULTIMATE_BANK_2",
    "ENCHANTMENT_ULTIMATE_BANK_3",
    "ENCHANTMENT_ULTIMATE_BANK_4",
    "ENCHANTMENT_ULTIMATE_BANK_5",
    "ENCHANTMENT_ULTIMATE_JERRY_1",
    "ENCHANTMENT_ULTIMATE_JERRY_2",
    "ENCHANTMENT_ULTIMATE_JERRY_3",
    "ENCHANTMENT_ULTIMATE_JERRY_4",
    "ENCHANTMENT_ULTIMATE_JERRY_5",
    "ENCHANTMENT_FEATHER_FALLING_6",
    "ENCHANTMENT_FEATHER_FALLING_7",
    "ENCHANTMENT_FEATHER_FALLING_8",
    "ENCHANTMENT_FEATHER_FALLING_9",
    "ENCHANTMENT_FEATHER_FALLING_10",
    "ENCHANTMENT_INFINITE_QUIVER_6",
    "ENCHANTMENT_INFINITE_QUIVER_7",
    "ENCHANTMENT_INFINITE_QUIVER_8",
    "ENCHANTMENT_INFINITE_QUIVER_9",
    "ENCHANTMENT_INFINITE_QUIVER_10",
    "SPIRIT_SHORTBOW",
    "SPIRIT_BOW",
    "ITEM_SPIRIT_BOW",
    "WITHER_BOOTS",
    "WITHER_CHESTPLATE",
    "WITHER_LEGGINGS",
    "WITHER_HELMET",
    "WITHER_CLOAK",
    "AUTO_RECOMBOBULATOR",
    "MASTER_SKULL_TIER_5",
    "MASTER_SKULL_TIER_4",
    "SHADOW_ASSASSIN_BOOTS",
    "SHADOW_ASSASSIN_LEGGINGS",
    "SHADOW_ASSASSIN_CHESTPLATE",
    "SHADOW_ASSASSIN_HELMET",
    "WARPED_STONE"
  )
    private set

  @JvmStatic
  var alwaysBuy = mutableListOf(
    "NECRON_HANDLE",
    "DARK_CLAYMORE",
    "FIRST_MASTER_STAR",
    "SECOND_MASTER_STAR",
    "THIRD_MASTER_STAR",
    "FOURTH_MASTER_STAR",
    "FIFTH_MASTER_STAR",
    "SHADOW_FURY",
    "SHADOW_WARP_SCROLL",
    "IMPLOSION_SCROLL",
    "WITHER_SHIELD_SCROLL",
    "DYE_LIVID"
  )
    private set

  @JvmStatic
  var runLog = mutableListOf<AutoCroesus.ChestInfo>()
    private set

  @JvmStatic
  fun load() {
    loadWorthless()
    loadAlwaysBuy()
    loadRunLog()
  }

  @JvmStatic
  fun saveWorthless() {
    FileUtils.writeJson(worthless, worthlessFile)
  }

  @OptIn(ExperimentalSerializationApi::class)
  private fun loadWorthless() {
    try {
      FileUtils.checkDir(worthlessFile, worthless)
      worthless = Json.decodeFromStream<MutableList<String>>(Files.newInputStream(worthlessFile.toPath()))
    } catch (_: Exception) {
      RSA.logger.error("Failed to read AutoCroesus worthless data!")
    }
  }

  @JvmStatic
  fun saveAlwaysBuy() {
    FileUtils.writeJson(alwaysBuy, alwaysBuyFile)
  }

  @OptIn(ExperimentalSerializationApi::class)
  private fun loadAlwaysBuy() {
    try {
      FileUtils.checkDir(alwaysBuyFile, alwaysBuy)
      alwaysBuy = Json.decodeFromStream<MutableList<String>>(Files.newInputStream(alwaysBuyFile.toPath()))
    } catch (_: Exception) {
      RSA.logger.error("Failed to read AutoCroesus always buy data!")
    }
  }

  @JvmStatic
  fun addRunLog(info: AutoCroesus.ChestInfo) {
    if (info.items.isEmpty() || info.value == 0.0 || runLog.contains(info)) return
    runLog.add(info)
    saveRunLog()
  }

  private fun saveRunLog() {
    FileUtils.writeJson(runLog, runLogFile)
  }

  @OptIn(ExperimentalSerializationApi::class)
  private fun loadRunLog() {
    try {
      FileUtils.checkDir(runLogFile, runLog)
      runLog = Json.decodeFromStream<MutableList<AutoCroesus.ChestInfo>>(Files.newInputStream(runLogFile.toPath()))
    } catch (_: Exception) {
      RSA.logger.error("Failed to read AutoCroesus run log data!")
    }
  }
}
