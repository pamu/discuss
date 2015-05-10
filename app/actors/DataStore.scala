package actors

import akka.actor.{Actor, ActorLogging}

/**
 * Created by pnagarjuna on 07/05/15.
 */

object DataStore {
  case class Entry(key: String, value: Any)
  case class Get(key: String)
  case class Evict(key: String)
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
  var data = Map.empty[String, Any]
  import DataStore._
  import Client._

  override def receive = {
    case Entry(key, value) =>
      log.info(s"${Entry(key, value)}")
      if (data contains(key)) {
        sender ! Error("Key exists")
      } else {
        sender ! Done(message = "Successfully added")
      }
    case Get(key) =>
      log.info(s"${Get(key)}")
      sender ! Value(message = "Success", data(key))
    case Evict(key) =>
      log.info(s"${Evict(key)}")
      data -= key
      sender ! Done(message = "Evicted")
    case x => log.info(s"unknown message of ${x.getClass}")
  }
}
