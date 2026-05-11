package rsa.event

import kotlin.reflect.KClass

interface Subscriber {
  fun <T : Event> subscribe(clazz: KClass<T>, priority: Int, callback: (T) -> Unit)
}

inline fun <reified T : Event> Subscriber.subscribe(priority: Int = 0, noinline callback: (T) -> Unit) =
  subscribe(T::class, priority, callback)

inline fun <reified T : Event> Subscriber.subscribe(noinline callback: (T) -> Unit, priority: Int = 0) =
  subscribe(T::class, priority, callback)
