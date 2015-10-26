package de.cas.rekoapp

import android.content.Intent
import android.os.{AsyncTask, Bundle}
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget._
import de.cas.rekoapp.backend.Projects
import de.cas.rekoapp.tasks.{CreateMeasureTask, EditMeasureTask, Task}
import de.cas.rekoapp.util.AndroidExtensions._

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class DetachedActivity extends AppCompatActivity {
    implicit val exec = ExecutionContext.fromExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

    object Ui {
        var titleText: TextView = null
        var textText: TextView = null
        var saveButton: Button = null
        var cancelButton: Button = null

        def initialize(tasks: ArrayAdapter[Task]) = {
            setContentView(R.layout.activity_detached)

            titleText = findViewById(R.id.titleText).asInstanceOf[TextView]
            textText = findViewById(R.id.textText).asInstanceOf[TextView]
            saveButton = findViewById(R.id.saveButton).asInstanceOf[Button]
            saveButton.onClick(finishTask)
            cancelButton = findViewById(R.id.cancelButton).asInstanceOf[Button]
            cancelButton.onClick(cancelTask)
            findViewById(R.id.attachButton).asInstanceOf[Button].onClick(switchToAttachedMode)

            val taskList = findViewById(R.id.detachedTaskList).asInstanceOf[ListView]
            taskList.setAdapter(tasks)
            taskList.onItemClick(startTask)

            hide()
        }

        def show(title: String, text: String) = {
            Seq(titleText, textText, saveButton, cancelButton).foreach(_.setVisibility(View.VISIBLE))
            titleText.setText(title)
            textText.setText(text)
        }

        def hide() = Seq(titleText, textText, saveButton, cancelButton).foreach(_.setVisibility(View.INVISIBLE))

        def title() = titleText.getText().toString()

        def text() = textText.getText().toString()
    }

    var tasks: java.util.List[Task] = null
    var currentTask: Option[Task] = null

    override def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        tasks = new java.util.ArrayList(SharedData.tasks)
        Ui.initialize(new ArrayAdapter[Task](this, android.R.layout.simple_list_item_1, tasks))
    }

    def startTask(index: Int): Unit = {
        currentTask = Some(tasks(index))
        tasks(index) match {
            case EditMeasureTask(_, measure, false) => Ui.show(measure.title, measure.text)
            case CreateMeasureTask(project, false) => Ui.show("", "")
        }
    }

    def cancelTask() =
        Ui.hide()

    def finishTask() = {
        val operation = currentTask match {
            case Some(EditMeasureTask(project, measure, _)) => Projects.saveMeasure(project, measure.copy(title = Ui.title(), text = Ui.text()))
            case Some(CreateMeasureTask(project, _)) => Projects.createMeasure(project, Ui.title(), Ui.text())
            case _ => null
        }

        operation.onComplete {
            case Success(_) =>
                runOnUiThread(new Runnable {
                    override def run(): Unit = {
                        val newTask = currentTask.get.finish()
                        val indexOfTask = tasks.indexOf(currentTask.get)
                        tasks.remove(indexOfTask)
                        tasks.add(indexOfTask, newTask)
                        currentTask = None
                        Ui.hide()
                        Toast.makeText(DetachedActivity.this, "Gespeichert", Toast.LENGTH_SHORT).show()
                    }
                })
            case Failure(error) =>
                println(error)
        }
    }

    def switchToAttachedMode() = {
        SharedData.tasks = tasks.toSeq

        val intent = new Intent(this, classOf[SyncedActivity])
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, findViewById(R.id.detachedTaskList), "taskList")
        startActivity(intent, options.toBundle)
        finish()
    }
}
