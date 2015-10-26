package de.cas.rekoapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget._
import de.cas.rekoapp.tasks.{CreateMeasureTask, EditMeasureTask, Task}

import scala.collection.JavaConversions._

class DetachedActivity extends AppCompatActivity {

    object Ui {

        def initialize(tasks: ArrayAdapter[Task]) = {
            setContentView(R.layout.activity_detached)

            val taskList = findViewById(R.id.detachedTaskList).asInstanceOf[ListView]
            taskList.setAdapter(tasks)
            taskList.setOnItemClickListener(new OnItemClickListener {
                override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = startTask(position)
            })
        }
    }

    var tasks: java.util.List[Task] = null

    override def onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        tasks = new java.util.ArrayList(SharedData.tasks)
        Ui.initialize(new ArrayAdapter[Task](this, android.R.layout.simple_list_item_1, tasks))
    }

    def startTask(index: Int) =
        tasks(index) match {
            case EditMeasureTask(project, measure) =>
            case CreateMeasureTask(project) =>
        }

}
