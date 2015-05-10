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
    dataStore ! DataStore.Entry("discussions", ListMap.empty[Long, String])
    dataStore ! DataStore.Entry("comments", ListMap.empty[Long, List[Long]])
  }

  override def onStop(app: Application): Unit = {
    super.onStop(app)
    Logger.info("App stopped")
    system shutdown()
  }
}
