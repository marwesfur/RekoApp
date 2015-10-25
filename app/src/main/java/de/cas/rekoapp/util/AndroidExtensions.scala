package de.cas.rekoapp.util

import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget.{AdapterView, ListView, Button}

object AndroidExtensions {

  implicit def toRunnable[F](f: => F): Runnable =
    new Runnable() { def run() = f }

  implicit def toPimpedButton(button: Button) = new {
    def onClick(f: () => Unit) = button.setOnClickListener(new OnClickListener {
      override def onClick(v: View): Unit = f()
    })
  }

  implicit def toPimpedListView(listView: ListView) = new {
    def onItemClick(f: Int => Unit) = listView.setOnItemClickListener(new OnItemClickListener {
      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = f(position)
    })
  }
}
