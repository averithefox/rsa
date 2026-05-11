package rsa.setting

import rsa.i18n.Translatable
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class Setting<T>(val defaultValue: T) : ReadWriteProperty<Configurable, T>,
  PropertyDelegateProvider<Configurable, ReadWriteProperty<Configurable, T>>, Translatable {
  var value = defaultValue

  private lateinit var owner: Configurable
  private lateinit var property: KProperty<*>

  private val fieldName get() = property.name

  override val typeName = "Setting"

  override fun getValue(thisRef: Configurable, property: KProperty<*>) = value

  override fun setValue(thisRef: Configurable, property: KProperty<*>, value: T) {
    this.value = value
  }

  override fun provideDelegate(thisRef: Configurable, property: KProperty<*>): ReadWriteProperty<Configurable, T> {
    owner = thisRef
    this.property = property
    owner.addSettings(this)
    return this
  }

  override fun getTranslation(keySuffix: String, vararg parameters: Any) =
    owner.getTranslation("$fieldName.$keySuffix", parameters)
}
