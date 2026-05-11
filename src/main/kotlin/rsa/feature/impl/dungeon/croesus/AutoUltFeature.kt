package rsa.feature.impl.dungeon.croesus

import rsa.event.impl.ChatEvent
import rsa.event.subscribe
import rsa.feature.Feature
import rsa.setting.Setting
import rsa.setting.impl.RangeSetting

object AutoUltFeature : Feature() {
  private val CHAT_COMMAND_PATTERN = Regex("^Party > (?:\\[.*] )?(.+): !wish")

  private val tankUltDelay by RangeSetting(15, 0..40)
  private val healerUltDelay by RangeSetting(3, 0..40)
  private val wishCommand by Setting(false)

  init {
    subscribe<ChatEvent.Chat> {

    }
  }
}
