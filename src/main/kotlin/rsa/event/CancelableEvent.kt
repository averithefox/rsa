package rsa.event

abstract class CancelableEvent : Event() {
  var isCanceled = false
    protected set

  fun cancel() {
    isCanceled = true
  }
}
