package actors

import akka.actor.{Actor, ActorLogging}

/**
 * Created by pnagarjuna on 07/05/15.
 */
object Counter {
  case object Discussion
  case object Comment
}

class Counter extends Actor with ActorLogging {
  var discussions = 0
  var comments = 0
  import Counter._
  override def receive = {
    case Discussion => discussions += 1
    case Comment => comments += 1
    case x => log.info(s"unknown message of type ${x.getClass}")
  }
}
