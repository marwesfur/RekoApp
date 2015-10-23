package de.cas.rekoapp.dispatcher


object Dispatcher {

  def subscribe(listener: (Event) => Unit) = {
    listener(ProjectOpened("456"))
  }

  def unsubscribe(listener: (Event) => Unit) = {

  }
}
