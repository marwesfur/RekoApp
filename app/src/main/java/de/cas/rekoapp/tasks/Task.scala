package de.cas.rekoapp.tasks

import de.cas.rekoapp.model.{ProjectMeasure, Project}

trait Task

case class CreateMeasureTask(project: Project) extends Task {
  override def toString() = "Neue Massnahme fuer " + project.title
}

case class EditMeasureTask(project: Project, measure: ProjectMeasure) extends Task {
  override def toString() = "Massnahme bearbeiten: " + measure.title
}
