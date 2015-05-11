package global

import actors.DataStore
import akka.actor.{Props, ActorSystem}
import play.api.{Logger, Application, GlobalSettings}

import scala.collection.immutable.ListMap

/**
 * Created by pnagarjuna on 07/05/15.
 */
object Global extends GlobalSettings {
  val system = ActorSystem("DataStore")
  lazy val dataStore = system.actorOf(Props[DataStore], "DataStore")

  override def onStart(app: Application): Unit = {
    super.onStart(app)
    Logger.info("App started")
    val discussions = ListMap[Long, String](1L -> "java", 2L -> "scala")
    dataStore ! DataStore.Entry("discussions", discussions)
    dataStore ! DataStore.Entry("discussion_1", List[String]("java is a multi purpose programming language"))
  }

  override def onStop(app: Application): Unit = {
    super.onStop(app)
    Logger.info("App stopped")
    system shutdown()
  }
}
