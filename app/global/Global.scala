package global

import akka.actor.{Props, ActorSystem}
import client.Client
import play.api.libs.json.{JsValue, Writes, Json}
import play.api.{Logger, Application, GlobalSettings}
import storage.Storage


/**
 * Created by pnagarjuna on 07/05/15.
 */
object Global extends GlobalSettings {
  val system = ActorSystem("ClusterSystem")
  val dataStore = system.actorOf(Props[Client], "Client")

  implicit val writes: Writes[(Long, String)] = new Writes[(Long, String)] {
    override def writes(o: (Long, String)): JsValue = {
      Json.obj("id" -> o._1, "headline" -> o._2)
    }
  }

  override def onStart(app: Application): Unit = {
    super.onStart(app)
    Logger.info("App started")
    val list = List((1L, "Scala"))
    //dataStore ! Storage.Entry("discussions", Json.stringify(Json.toJson(list)))
  }

  override def onStop(app: Application): Unit = {
    super.onStop(app)
    Logger.info("App stopped")
    system shutdown()
  }
}
