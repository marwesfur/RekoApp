package de.cas.rekoapp.dispatcher


object Dispatcher {

  def subscribe(listener: (Event) => Unit) = {
    listener(ProjectOpened("4B139E277378314FA53D121A9CC4C2F1"))
  }

  def unsubscribe(listener: (Event) => Unit) = {

  }
}
