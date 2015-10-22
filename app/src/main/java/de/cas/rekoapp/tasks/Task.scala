package de.cas.rekoapp.tasks

import de.cas.rekoapp.model.{ProjectMeasure, Project}

trait Task

case class CreateMeasureTask(project: Project) extends Task
case class EditMeasureTask(project: Project, measure: ProjectMeasure) extends Task
