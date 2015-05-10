package global

import actors.{Counter, DataStore}
import akka.actor.{Props, ActorSystem}
import play.api.{Logger, Application, GlobalSettings}

/**
 * Created by pnagarjuna on 07/05/15.
 */
object Global extends GlobalSettings {
  val system = ActorSystem("DataStore")
  lazy val dataStore = system.actorOf(Props[DataStore], "DataStore")
  lazy val counter = system.actorOf(Props[Counter], "Counter")

  override def onStart(app: Application): Unit = {
    super.onStart(app)
    Logger.info("App started")
    import DataStore._

    val discussions = List[String]
    dataStore ! Entry(1, discussions)
    val comments = Map[Long, List[String]]
    dataStore ! Entry(2, comments)
  }

  override def onStop(app: Application): Unit = {
    super.onStop(app)
    Logger.info("App stopped")
    system shutdown()
  }
}
