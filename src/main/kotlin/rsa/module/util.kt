package rsa.module

import com.ricedotwho.rsm.module.Module
import com.ricedotwho.rsm.module.SubModule
import com.ricedotwho.rsm.ui.clickgui.settings.Setting
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting
import kotlin.reflect.KProperty

operator fun <T : Module> DragSetting.getValue(thisRef: SubModule<T>, property: KProperty<*>): DragSetting {
  return this
}

operator fun <T, S : Setting<T>> S.provideDelegate(thisRef: Module, property: KProperty<*>): S {
  thisRef.registerProperty(this)
  return this
}

operator fun <T, S : Setting<T>> S.getValue(thisRef: Module, property: KProperty<*>): T {
  return value
}

operator fun <T, S : Setting<T>, U : Module> S.provideDelegate(thisRef: SubModule<U>, property: KProperty<*>): S {
  thisRef.registerProperty(this)
  return this
}

operator fun <T, S : Setting<T>, U : Module> S.getValue(thisRef: SubModule<U>, property: KProperty<*>): T {
  return value
}
