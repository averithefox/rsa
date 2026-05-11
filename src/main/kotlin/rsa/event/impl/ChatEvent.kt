package rsa.event.impl

import net.minecraft.network.chat.Component
import rsa.event.Event

object ChatEvent {
  class ActionBar(val component: Component) : Event()
  class Chat(val component: Component) : Event()
}
