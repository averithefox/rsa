package rsa.features

import rsa.events.Event
import rsa.events.EventBus
import rsa.events.Subscriber

abstract class Feature : Subscriber {
  var enabled: Boolean = false

  final override fun <T : Event> subscribe(clazz: Class<T>, priority: Int, callback: (T) -> Unit) =
    EventBus.subscribe(clazz, priority) {
      if (enabled) callback(it);
    }
}
