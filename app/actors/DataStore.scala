package actors

import akka.actor.{Actor, ActorLogging}
import akka.io.Tcp.Message

/**
 * Created by pnagarjuna on 07/05/15.
 */

object DataStore {
  case class Entry(id: Long, value: Any)
  case class Get(id: Long)
  case class Evict(id: Long)
}

object Client {
  trait Result {
    val message: String
  }
  case class Value(message: String, value: Any) extends Result
  case class Done(message: String) extends Result
  case class Error(message: String) extends Result
}

class DataStore extends Actor with ActorLogging {
  var data = Map.empty[Long, Any]
  import DataStore._
  import Client._

  override def receive = {
    case Entry(id, value) =>
      log.info(s"${Entry(id, value)}")
      data += (id -> value)
      sender ! Done(message = "successfully added")
    case Get(id) =>
      log.info(s"${Get(id)}")
      sender ! Value(message = "success", data(id))
    case Evict(id) =>
      log.info(s"${Evict(id)}")
      data -= id
      sender ! Done(message = "Evicted")
    case x => log.info(s"unknown message of ${x.getClass}")
  }
}
