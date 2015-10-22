package de.cas.rekoapp.dispatcher


object Dispatcher {



  def subscribe(listener: (Event) => Unit): Option[ProjectOpened] = {
    None
  }

  def unsubscribe(listener: (Event) => Unit) = {

  }
}
