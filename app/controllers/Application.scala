package controllers

import actors.Client.{Error, Done, Result, Value}
import actors.DataStore
import global.Global
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

import scala.collection.immutable.ListMap
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
        val future = (Global.dataStore ? DataStore.Get("discussions")).mapTo[Result]
        future.flatMap { result => {
          result match {
            case value: Value => {
              val listMap: ListMap[Long, String] = value.value.asInstanceOf[ListMap[Long, String]]
              val newListMap = listMap + ((listMap.size + 1).toLong -> discussion)
              val f = (Global.dataStore ? DataStore.Update("discussions", newListMap)).mapTo[Result]
              Global.dataStore ! DataStore.Update("discussion_"+newListMap.size, List[String]("Start Commenting ... :)"))
              f.map { result => {
                result match {
                  case Done(msg) => Ok(Json.obj("success" -> msg))
                  case Error(msg) => Ok(Json.obj("failure" -> msg))
                }
              }}
            }
            case error: Error => Future(Ok(Json.obj("failure" -> error.message)))
          }

        }}
      }
      case error: JsError => {
        Future(Ok(Json.obj("failure" -> "bad json format")))
      }
    }
  }

  def discussions() = Action.async {implicit request => {
    implicit val writes: Writes[(Long, String)] = new Writes[(Long, String)] {
      override def writes(o: (Long, String)): JsValue = {
        Json.obj("id" -> o._1, "headline" -> o._2)
      }
    }
    val future = (Global.dataStore ? DataStore.Get("discussions")).mapTo[Value]
    future.map(value => {
      val pairs = value.value.asInstanceOf[ListMap[Long, String]].toList
      Ok(Json.obj("discussions" -> pairs))
    }).recover{case throwable: Throwable => Ok(Json.obj("error" -> throwable.getMessage))}
  }}

  def discuss(id: Long) = Action.async { implicit request =>
    val future = (Global.dataStore ? DataStore.Get("discussions")).mapTo[Value]
    future.flatMap {value => {
      val listMap: ListMap[Long, String] = value.value.asInstanceOf[ListMap[Long, String]]
      Future(Ok(views.html.discuss(listMap(id), id)))
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
        val f = (Global.dataStore ? DataStore.Get("discussion_" + success.get.did)).mapTo[Result]
        f.flatMap{result => {
          result match {
            case value: Value => {
              val newList = value.value.asInstanceOf[List[String]] ++ List(success.get.comment)
              Logger.info("play log" + newList.mkString(" "))
              val future = (Global.dataStore ? DataStore.Update("discussion_" + success.get.did, newList)).mapTo[Result]
              future.flatMap { result => {
                result match {
                  case Done(msg) =>  Future(Ok(Json.obj("done" -> msg)))
                  case Error(msg) => Future(Ok(Json.obj("error" -> msg)))
                }
              }}
            }
            case error: Error => Future(Ok(Json.obj("error" -> error.message)))
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
    val f = (Global.dataStore ? DataStore.Get(key)).mapTo[Result]
    f.flatMap {result => {
      result match {
        case value: Value => {
          val list = value.value.asInstanceOf[List[String]]
          Future(Ok(Json.obj("comments" -> list)))
        }
        case error: Error => Future(Ok(Json.obj("error" -> error.message)))
      }
    }}.recover{ case throwable: Throwable => Ok(Json.obj("error" -> throwable.getMessage))}
  }}
 }
