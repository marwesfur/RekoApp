package de.cas.rekoapp.tasks

import de.cas.rekoapp.model.{ProjectMeasure, Project}

trait Task {
  val done: Boolean
  def finish(): Task
}

case class CreateMeasureTask(project: Project, done: Boolean) extends Task {
  override def toString() = (if (done) "Erledigt: " else "") + "Neue Massnahme fuer " + project.title

  def finish() = copy(done = true)
}

case class EditMeasureTask(project: Project, measure: ProjectMeasure, done: Boolean) extends Task {
  override def toString() = (if (done) "Erledigt: " else "") + "Massnahme bearbeiten: " + measure.title

  def finish() = copy(done = true)
}
