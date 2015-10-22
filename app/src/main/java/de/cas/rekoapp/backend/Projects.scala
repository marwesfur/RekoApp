package de.cas.rekoapp.backend

import de.cas.rekoapp.model.{ProjectMeasure, Project}

object Projects {

  val projects = Seq(Project("123", "Auto", Seq()), Project("456", "Fahrrad", Seq(ProjectMeasure("789", "Aufpumpen", "Keine Luft drin"))))

  def byId(guid: String): Option[Project] =
    projects.find(_.guid.equals(guid))
}