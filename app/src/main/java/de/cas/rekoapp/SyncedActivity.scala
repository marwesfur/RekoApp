package de.cas.rekoapp

import java.util

import android.content.Intent
import android.support.v4.app.ActivityOptionsCompat

import scala.collection.JavaConversions._
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget._
import de.cas.rekoapp.backend.Projects
import de.cas.rekoapp.dispatcher.{Dispatcher, Event, ProjectClosed, ProjectOpened}
import de.cas.rekoapp.model.{Project, ProjectMeasure}
import de.cas.rekoapp.tasks.{CreateMeasureTask, EditMeasureTask, Task}

class SyncedActivity extends AppCompatActivity {

    object Ui {
        var projectTitleText: TextView = null
        var addMeasureButton: Button = null
        var existingMeasureList: ListView = null

        def initialize(tasks: ArrayAdapter[Task], existingMeasures: ArrayAdapter[ProjectMeasure]) = {
            setContentView(R.layout.activity_synced)

            projectTitleText = findViewById(R.id.projectTitle).asInstanceOf[TextView]
            addMeasureButton = findViewById(R.id.addMeasure).asInstanceOf[Button]
            addMeasureButton.setOnClickListener(new OnClickListener {
                override def onClick(v: View): Unit = addCreateMeasureTask()
            })
            existingMeasureList = findViewById(R.id.existingMeasureList).asInstanceOf[ListView]
            existingMeasureList.setAdapter(existingMeasures)
            existingMeasureList.setOnItemClickListener(new OnItemClickListener {
                override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = addEditMeasureTask(position)
            })
            val taskList = findViewById(R.id.taskList).asInstanceOf[ListView]
            taskList.setAdapter(tasks)

            findViewById(R.id.detachButton).asInstanceOf[Button].setOnClickListener(new OnClickListener {
                override def onClick(v: View): Unit = switchToDetachedMode()
            })

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
    var tasks: util.List[Task] = null
    var existingMeasuresAdapter: ArrayAdapter[ProjectMeasure] = null
    var tasksAdapter: ArrayAdapter[Task] = null


    override def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        tasks = new util.ArrayList(SharedData.tasks)
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
        syncedProject = Projects.byId(guid)
        syncedProject.foreach { project =>
            existingMeasuresAdapter.clear()
            existingMeasuresAdapter.addAll(project.measures)
            Ui.openProject(project)
        }
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
