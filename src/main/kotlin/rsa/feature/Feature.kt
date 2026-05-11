package rsa.feature

import rsa.event.Event
import rsa.event.EventBus
import rsa.event.Subscriber
import rsa.i18n.Translatable
import rsa.setting.Configurable
import rsa.setting.Setting
import kotlin.reflect.KClass

abstract class Feature : Configurable, Translatable, Subscriber {
  var enabled: Boolean = false

  final override val settings: List<Setting<*>>
    field = mutableListOf()

  final override val typeName = "Feature"

  final override fun addSettings(vararg settings: Setting<*>) {
    this.settings.addAll(settings)
  }

  final override fun <T : Event> subscribe(clazz: KClass<T>, priority: Int, callback: (T) -> Unit) =
    EventBus.subscribe(clazz, priority) {
      if (enabled) callback(it);
    }
}
