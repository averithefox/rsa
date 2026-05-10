package rsa.utils

import net.minecraft.network.chat.Component
import rsa.RSA

object ChatUtils {
  @JvmStatic
  fun chat(component: Component) {
    chatClean(RSA.prefix.append(component))
  }

  @JvmStatic
  private fun chatClean(message: Component) {
    mc.player?.let { mc.execute { mc.gui.chat.addMessage(message) } }
  }
}
