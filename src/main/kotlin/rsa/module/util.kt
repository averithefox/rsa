package rsa.module

import com.ricedotwho.rsm.module.Module
import com.ricedotwho.rsm.ui.clickgui.settings.Setting
import kotlin.reflect.KProperty

operator fun <T, S : Setting<T>> S.provideDelegate(thisRef: Module, property: KProperty<*>): S {
  thisRef.registerProperty(this)
  return this
}

operator fun <T, S : Setting<T>> S.getValue(thisRef: Module, property: KProperty<*>): T {
  return value
}

operator fun <T, S : Setting<T>> S.setValue(thisRef: Module, property: KProperty<*>, value: T) {
  this.value = value
}
