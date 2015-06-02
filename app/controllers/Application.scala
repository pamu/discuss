package controllers

import client.Client
import Client.{SuccessMessage, Data, FailureMessage, ClientMessage}
import storage.Storage
import Storage.{Entry, Get}
import global.Global._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import storage.Storage

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import play.api.libs.functional.syntax._


object Application extends Controller {

  implicit val timeout = Timeout(5 seconds)

  def index = Action {
    Ok(views.html.index("Discuss"))
  }
  case class Discussion(name: String)
  implicit val discussionReads: Reads[Discussion] = (
    (JsPath \ "name").read[String].map(Discussion(_))
    )

  def discussion() = Action.async(parse.json) { implicit request =>
    request.body.validate[Discussion] match {
      case success: JsSuccess[Discussion] => {
        val discussion = success.get.name
        val f = (dataStore ? Get("discussions")).mapTo[ClientMessage]
        f.flatMap { clientMessage =>
          clientMessage match {
            case Data(key, value) => {
              Json.parse(value).validate[List[(Long, String)]] match {
                case success: JsSuccess[List[(Long, String)]] => {
                  val list = success.get
                  val count = list.size + 1
                  val dis = list ++ List((count.toLong, discussion))
                  val f = (dataStore ? Entry(key, Json.stringify(Json.toJson(dis))))
                  f.flatMap { clientMessage =>
                    clientMessage match {
                      case SuccessMessage(key, msg) => {
                        val f = (dataStore ? Entry("discussion_" + count, Json.stringify(Json.toJson(List("comment here")))))
                        f.flatMap {result =>
                          result match {
                            case SuccessMessage(key, msg) => Future(Ok(Json.obj("success" -> "operation successful.")))
                            case FailureMessage(key, msg) => Future(Ok(Json.obj("error" -> "Found not create comments key.")))
                          }
                        }
                      }
                      case FailureMessage(key, msg) =>
                        Future(Ok(Json.obj("error" -> "operation failed.")))
                    }
                  }
                }
                case error: JsError => Future(Ok(Json.obj("error" -> "Internal Json format in bad shape.")))
              }
            }
            case FailureMessage(key, msg) => {
              Future(Ok(Json.obj("error" -> s"reason: ${msg}")))
            }
          }
        }
      }
      case error: JsError => {
        Future(Ok(Json.obj("error" -> "bad json format")))
      }
    }
  }

  implicit val writes: Writes[(Long, String)] = new Writes[(Long, String)] {
    override def writes(o: (Long, String)): JsValue = {
      Json.obj("id" -> o._1, "headline" -> o._2)
    }
  }

  implicit val reads: Reads[(Long, String)] = new Reads[(Long, String)] {
    override def reads(json: JsValue): JsResult[(Long, String)] = {
      for {
        id <- (json \ "id").validate[Long]
        headline <- (json \ "headline").validate[String]
      } yield (id, headline)
    }
  }

  def discussions() = Action.async {implicit request => {

    val future = (dataStore ? Get("discussions")).mapTo[ClientMessage]
    future.map(value => {
      value match {
        case Data(key, value) => {
          Ok(Json.obj("discussions" -> Json.parse(value)))
        }
        case FailureMessage(key, msg) => Ok(Json.obj("error" -> "operation failed."))
      }
    }).recover{case throwable: Throwable => Ok(Json.obj("error" -> throwable.getMessage))}
  }}

  def discuss(id: Long) = Action.async { implicit request =>
    val future = (dataStore ? Get("discussions")).mapTo[ClientMessage]
    future.flatMap {value => {
      value match {
        case Data(key, value) => {
          Json.parse(value).validate[List[(Long, String)]] match {
            case success: JsSuccess[List[(Long, String)]] => {
              val listMap = success.get.toMap
              Future(Ok(views.html.discuss(listMap(id), id)))
            }
            case error: JsError => Future(Ok(Json.obj("error" -> "internal json parsing error")))
          }
        }
        case FailureMessage(key, value) => Future(Ok(Json.obj("error" -> s"Failed reason ${value}")))
      }
    }}
  }
  case class Comment(did: Long, comment: String)
  implicit val commentReads: Reads[Comment] = (
    (JsPath \ "did").read[Long] and
      (JsPath \ "comment").read[String]
    )(Comment.apply _)
  def comment() = Action.async(parse.json) { implicit request =>
    request.body.validate[Comment] match {
      case success: JsSuccess[Comment] => {
        val com = success.get.comment
        val did = success.get.did
        val f = (dataStore ? Get("discussion_" + success.get.did)).mapTo[ClientMessage]
        f.flatMap{result => {
          result match {
            case Data(key, value) => {
              Json.parse(value).validate[List[String]] match {
                case success: JsSuccess[List[String]] => {
                  val list = success.get
                  val newList = list ++ List(com)
                  Logger.info("play log" + newList.mkString(" "))
                  val future = (dataStore ? Entry("discussion_" + did, Json.stringify(Json.toJson(newList)))).mapTo[ClientMessage]
                  future.flatMap { result => {
                    result match {
                      case SuccessMessage(key, msg) =>  Future(Ok(Json.obj("done" -> msg)))
                      case FailureMessage(key, msg) => Future(Ok(Json.obj("error" -> msg)))
                    }
                  }}
                }
                case error: JsError => Future(Ok(Json.obj("error" -> "internal json parsing error")))
              }
            }
            case FailureMessage(key, msg) => Future(Ok(Json.obj("error" -> s"failed reason ${msg}")))
          }
        }}

      }
      case error: JsError => {
        Future(Ok(Json.obj("error" -> "json format not accepted")))
      }
    }
  }

  def comments(id: Long) = Action.async { implicit request => {
    val key = "discussion_"+id
    val f = (dataStore ? Get(key)).mapTo[ClientMessage]
    f.flatMap {result => {
      result match {
        case Data(key, value) => {
          Json.parse(value).validate[List[String]] match {
            case success: JsSuccess[List[String]] => {
              Future(Ok(Json.obj("comments" -> success.get)))
            }
            case error: JsError => Future(Ok(Json.obj("error" -> "internal json parsing error")))
          }
        }
        case FailureMessage(key, msg) => Future(Ok(Json.obj("error" -> msg)))
      }
    }}.recover{ case throwable: Throwable => Ok(Json.obj("error" -> throwable.getMessage))}
  }}
 }
