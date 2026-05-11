package rsa.event

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

object EventBus : Subscriber {
  private val listeners = ConcurrentHashMap<KClass<out Event>, CopyOnWriteArrayList<Listener>>()

  override fun <T : Event> subscribe(clazz: KClass<T>, priority: Int, callback: (T) -> Unit) {
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
    listeners[event::class]?.forEach { listener ->
      listener.callback(event)
      if (event is CancelableEvent && event.isCanceled) return true
    }

    return false
  }

  private data class Listener(val priority: Int, val callback: (Event) -> Unit)
}
