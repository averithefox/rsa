package rsa.setting.impl

import rsa.setting.Setting

class RangeSetting(defaultValue: Float, val range: ClosedFloatingPointRange<Float>, val step: Float) :
  Setting<Float>(defaultValue) {
  constructor(defaultValue: Int, range: IntRange) : this(
    defaultValue.toFloat(),
    range.first.toFloat()..range.last.toFloat(),
    range.step.toFloat()
  )
}
