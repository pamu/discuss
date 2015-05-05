package actors

import akka.actor.{Actor, ActorLogging}

/**
 * Created by pnagarjuna on 05/05/15.
 */
object Storage {
  case class Add(text: String)
  case class Remove(id: Long)
  case class Get(id: Long)
}

class Storage extends Actor with ActorLogging {
  override def receive = {
    case _ =>
  }
}
