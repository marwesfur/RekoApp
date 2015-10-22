package de.cas.rekoapp

import java.util

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView.OnItemClickListener
import android.widget._
import de.cas.rekoapp.backend.Projects
import de.cas.rekoapp.dispatcher.{ProjectClosed, ProjectOpened, Event, Dispatcher}
import de.cas.rekoapp.model.Project
import de.cas.rekoapp.tasks.{EditMeasureTask, CreateMeasureTask, Task}

class SyncedActivity extends AppCompatActivity {

    object Ui {
        var projectTitleText: TextView = null
        var addMeasureButton: Button = null
        var existingMeasureList: ListView = null
        var taskList: ListView = null

        def initialize() = {
            setContentView(R.layout.activity_synced)

            projectTitleText = findViewById(R.id.projectTitle).asInstanceOf[TextView]
            addMeasureButton = findViewById(R.id.addMeasure).asInstanceOf[Button]
            existingMeasureList = findViewById(R.id.existingMeasureList).asInstanceOf[ListView]
            taskList = findViewById(R.id.taskList).asInstanceOf[ListView]

            addMeasureButton.setOnClickListener(new OnClickListener {
                override def onClick(v: View): Unit = addCreateMeasureTask()
            })

            taskList.setAdapter(new ArrayAdapter[Task](this, R.id.addMeasure, tasks))

            existingMeasureList.setOnItemClickListener(new OnItemClickListener {
                override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = addEditMeasureTask(position)
            })

            closeProject()
        }

        def openProject(project: Project) =
            Seq(projectTitleText, addMeasureButton, existingMeasureList).foreach(_.setVisibility(View.VISIBLE))

        def closeProject() =
            Seq(projectTitleText, addMeasureButton, existingMeasureList).foreach(_.setVisibility(View.INVISIBLE))
    }

    var syncedProject: Option[Project] = None
    val tasks: util.List[Task] = new util.ArrayList[Task]()

    override def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        Ui.initialize()

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
        syncedProject.foreach(Ui.openProject)
    }

    def closeProject() = {
        syncedProject = None
        Ui.closeProject()
    }

    def addCreateMeasureTask() =
        tasks.add(CreateMeasureTask(syncedProject.get))

    def addEditMeasureTask(index: Int) =
        tasks.add(EditMeasureTask(syncedProject.get, syncedProject.get.measures(index)))
}