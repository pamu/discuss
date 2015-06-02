package client

import akka.actor.{Actor, ActorLogging, RelativeActorPath, RootActorPath}
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, MemberStatus, UniqueAddress}
import storage.Storage


object Client {
  trait ClientMessage
  case class Data(key: String, value: String) extends ClientMessage
  case class SuccessMessage(key: String, msg: String) extends ClientMessage
  case class FailureMessage(key: String, msg: String) extends ClientMessage
}

class Client extends Actor with ActorLogging {
  import Client._
  import Storage._
  var nodes = Set.empty[UniqueAddress]
  val cluster = Cluster(context.system)
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp], classOf[UnreachableMember], classOf[ReachabilityEvent])
  override def postStop(): Unit = cluster.unsubscribe(self)
  override def receive = {
    case msg: StorageMessage =>
      log.info(nodes.mkString("\n", "\n", "\n"))
      if (nodes.size < 1) {
        log.info("Cannot send message no nodes registered.")
      } else {
        val addr = nodes.filter(_ != cluster.selfUniqueAddress).toIndexedSeq(0)
        val service = context.actorSelection(RootActorPath(addr.address) / ("user/Service" match {case RelativeActorPath(elements) => elements}))
        service forward msg
      }
    case msg: ClientMessage =>
      msg match {
        case Data(key, value) => log.info("Got Data {}", Data(key, value))
        case SuccessMessage(key, msg) => log.info("Message for {} {}", key, msg)
        case FailureMessage(key, msg) => log.info("Message for {} {}", key, msg)
      }
    case state: CurrentClusterState => state.members.collect {
      case member if member.hasRole("storage") && member.status == MemberStatus.Up =>
        log.info("Node with address {} is Up", member.address)
    }
    case MemberUp(member) =>
      log.info("adding nodes to set " + member)
      nodes += member.uniqueAddress
    case ReachableMember(member) =>
      log.info("adding nodes to set " + member)
      nodes += member.uniqueAddress
    case UnreachableMember(member) => nodes -= member.uniqueAddress
    case other: MemberEvent => //nodes -= other.member.uniqueAddress
    case ex => log.info("Message {} of type {}", ex, ex getClass)
  }
}
