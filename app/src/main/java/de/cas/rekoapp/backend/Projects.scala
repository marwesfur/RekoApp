package de.cas.rekoapp.backend

import android.os.AsyncTask
import de.cas.rekoapp.model.{Project, ProjectMeasure}
import dispatch.{Http, _}
import net.liftweb.json.DefaultFormats
import net.liftweb.json
import net.liftweb.json.JsonAST.JArray
import net.liftweb.json.JsonDSL._

import scala.concurrent.{ExecutionContext, Future}

object Projects {
  implicit val formats = DefaultFormats
  implicit val exec = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

  val myHost = host("172.24.59.212", 8080)

  def byId(guid: String): Future[Project] =
    // https://groups.google.com/d/topic/dispatch-scala/fUmU6mNHbjc/discussion
    // AsyncHttpClient seems to do i/o on the calling thread => wrapping it in a Future moves it to a background thread
    Future { Http(myHost / "projects" / guid OK as.String) }
      .flatMap(identity)
      .map { result =>
        // arrggghh: lift-json's extract[Project] probably won't work on android (http://stackoverflow.com/questions/6520592/problem-extracting-json-using-lift-json-in-an-android-scala-application)
        // todo: figure out an alternative.
        val jsonObj = json.parse(result)
        val measures = for {
          JArray(measures) <- jsonObj \ "measures"
          measure <- measures
        } yield ProjectMeasure((measure \ "guid").extract[String], (measure \ "title").extract[String], (measure \ "text").extract[String])

        Project((jsonObj \ "guid").extract[String], (jsonObj \ "title").extract[String], measures)
      }

  def createMeasure(project: Project, title: String, text: String): Future[Unit] = {
    val body = ("title" -> title) ~ ("text" -> text)
    val request = (myHost / "projects" / project.guid / "measures")
      .setContentType("application/json", "UTF-8") << json.compactRender(body)

    Future { Http(request OK as.String) }.flatMap(identity).map(_ => Unit)
  }

  def saveMeasure(project: Project, measure: ProjectMeasure): Future[Unit] = {
    val body = ("guid" -> measure.guid) ~ ("title" -> measure.title) ~ ("text" -> measure.text)
    val request = (myHost / "projects" / project.guid / "measures" / measure.guid)
      .setContentType("application/json", "UTF-8") << json.compactRender(body)

    Future { Http(request OK as.String) }.flatMap(identity).map(_ => Unit)
  }
}