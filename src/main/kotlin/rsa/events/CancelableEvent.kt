package rsa.events

abstract class CancelableEvent : Event() {
  var isCanceled = false
    protected set

  fun cancel() {
    isCanceled = true
  }
}
