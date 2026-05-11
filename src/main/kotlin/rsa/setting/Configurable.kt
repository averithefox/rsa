package rsa.setting

import rsa.i18n.Translatable

interface Configurable : Translatable {
  val settings: List<Setting<*>>

  fun addSettings(vararg settings: Setting<*>)
}
