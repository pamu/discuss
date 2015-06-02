package storage

/**
  * Created by pnagarjuna on 02/06/15.
  */
object Storage {
   trait StorageMessage
   case class Get(key: String) extends StorageMessage
   case class Entry(key: String, value: String) extends StorageMessage
   case class Evict(key: String) extends StorageMessage

   trait ReplicaMessage
   case class ReplicaEntry(key: String, value: String) extends ReplicaMessage
   case class ReplicaEvict(key: String) extends ReplicaMessage
 }
