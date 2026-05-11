package rsa.i18n

import com.google.common.base.CaseFormat
import net.minecraft.client.resources.language.I18n

interface Translatable {
  val typeName: String
  val translationKeyName
    get() = this::class.simpleName?.replace(typeName, "")?.let {
      CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, it)
    } ?: "null"

  fun getTranslation(keySuffix: String, vararg parameters: Any): String {
    return I18n.get("${typeName.lowercase()}.rsa.$translationKeyName.$keySuffix", parameters)
  }

  val translatedName get() = getTranslation("name")
}
