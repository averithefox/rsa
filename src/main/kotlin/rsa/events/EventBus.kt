package rsa.events

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

// todo: use KClass after everything gets rewritten in kotlin
object EventBus : Subscriber {
  private val listeners = ConcurrentHashMap<Class<out Event>, CopyOnWriteArrayList<Listener>>()

  override fun <T : Event> subscribe(clazz: Class<T>, priority: Int, callback: (T) -> Unit) {
    @Suppress("unchecked_cast") val listener = Listener(priority, callback as ((Event) -> Unit))

    val typedListeners = listeners.computeIfAbsent(clazz) { CopyOnWriteArrayList() }
    typedListeners.add(listener)

    typedListeners.sortByDescending { it.priority }
  }

  /**
   * @return did the event get canceled
   */
  @JvmStatic
  fun <T : Event> publish(event: T): Boolean {
    listeners[event::class.java]?.forEach { listener ->
      listener.callback(event)
      if (event is CancelableEvent && event.isCanceled) return true
    }

    return false
  }

  private data class Listener(val priority: Int, val callback: (Event) -> Unit)
}
