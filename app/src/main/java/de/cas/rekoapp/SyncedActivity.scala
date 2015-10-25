package de.cas.rekoapp

import android.content.Intent
import android.os.{AsyncTask, Bundle}
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget._
import de.cas.rekoapp.dispatcher.{Dispatcher, Event, ProjectClosed, ProjectOpened}
import de.cas.rekoapp.model.{Project, ProjectMeasure}
import de.cas.rekoapp.tasks.{CreateMeasureTask, EditMeasureTask, Task}
import de.cas.rekoapp.util.AndroidExtensions._
import dispatch._
import net.liftweb.json
import net.liftweb.json.DefaultFormats

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext

class SyncedActivity extends AppCompatActivity {

    implicit val formats = DefaultFormats
    implicit val exec = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

    object Ui {
        var projectTitleText: TextView = null
        var addMeasureButton: Button = null
        var existingMeasureList: ListView = null

        def initialize(tasks: ArrayAdapter[Task], existingMeasures: ArrayAdapter[ProjectMeasure]) = {
            setContentView(R.layout.activity_synced)
            projectTitleText = findViewById(R.id.projectTitle).asInstanceOf[TextView]
            addMeasureButton = findViewById(R.id.addMeasure).asInstanceOf[Button]
            addMeasureButton.onClick(addCreateMeasureTask)
            existingMeasureList = findViewById(R.id.existingMeasureList).asInstanceOf[ListView]
            existingMeasureList.setAdapter(existingMeasures)
            existingMeasureList.onItemClick(addEditMeasureTask)
            findViewById(R.id.taskList).asInstanceOf[ListView].setAdapter(tasks)
            findViewById(R.id.detachButton).asInstanceOf[Button].onClick(switchToDetachedMode)

            closeProject()
        }

        def openProject(project: Project) = {
            Seq(projectTitleText, addMeasureButton, existingMeasureList).foreach(_.setVisibility(View.VISIBLE))
            projectTitleText.setText(project.title)
        }

        def closeProject() =
            Seq(projectTitleText, addMeasureButton, existingMeasureList).foreach(_.setVisibility(View.INVISIBLE))
    }

    var syncedProject: Option[Project] = None
    var tasks: java.util.List[Task] = null
    var existingMeasuresAdapter: ArrayAdapter[ProjectMeasure] = null
    var tasksAdapter: ArrayAdapter[Task] = null


    override def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        tasks = new java.util.ArrayList(SharedData.tasks)
        tasksAdapter = new ArrayAdapter[Task](this, android.R.layout.simple_list_item_1, tasks)
        existingMeasuresAdapter = new ArrayAdapter[ProjectMeasure](this, android.R.layout.simple_list_item_1)
        Ui.initialize(tasksAdapter, existingMeasuresAdapter)

        Dispatcher.subscribe(onMainApplicationEvent)
    }

    def onMainApplicationEvent(event: Event) =
        event match {
            case ProjectOpened(guid) => openProject(guid)
            case ProjectClosed => closeProject()
            case _ =>
        }

    def openProject(guid: String) = {
        // https://groups.google.com/d/topic/dispatch-scala/fUmU6mNHbjc/discussion
        // AsyncHttpClient seems to do i/o on the calling thread => wrapping it in a Future moves it to a background thread
        Future { Http(url("http://localhost:8080/projects/" + guid) OK as.String) }
          .flatMap(identity)
          .foreach { result =>
              val project = json.parse(result).extract[Project]
              runOnUiThread(() => {
                  existingMeasuresAdapter.clear()
                  existingMeasuresAdapter.addAll(project.measures)
                  Ui.openProject(project)
              })
          }

//        syncedProject = Projects.byId(guid)
//        syncedProject.foreach { project =>
//            existingMeasuresAdapter.clear()
//            existingMeasuresAdapter.addAll(project.measures)
//            Ui.openProject(project)
//        }
    }

    def closeProject() = {
        syncedProject = None
        existingMeasuresAdapter.clear()
        Ui.closeProject()
    }

    def addCreateMeasureTask() =
        tasksAdapter.add(CreateMeasureTask(syncedProject.get))

    def addEditMeasureTask(index: Int) =
        tasksAdapter.add(EditMeasureTask(syncedProject.get, syncedProject.get.measures(index)))

    // https://github.com/codepath/android_guides/wiki/Shared-Element-Activity-Transition
    def switchToDetachedMode() = {
        SharedData.tasks = tasks.toSeq

        val intent = new Intent(this, classOf[DetachedActivity])
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, findViewById(R.id.taskList), "taskList")
        startActivity(intent, options.toBundle)
    }
}
