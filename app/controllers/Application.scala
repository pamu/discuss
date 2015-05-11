package controllers

import actors.Client.{Error, Done, Result, Value}
import actors.DataStore
import global.Global
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
        val future = (Global.dataStore ? DataStore.Get("discussions")).mapTo[Value]
        future.flatMap { value => {
          val listMap: ListMap[Long, String] = value.value.asInstanceOf[ListMap[Long, String]]
          val newListMap = listMap + ((listMap.size + 1).toLong -> discussion)
          val f = (Global.dataStore ? DataStore.Update("discussions", newListMap)).mapTo[Result]
          f.map { result => {
            result match {
              case Done(msg) => Ok(Json.obj("success" -> msg))
              case Error(msg) => Ok(Json.obj("failure" -> msg))
            }
          }}
        }}
      }
      case error: JsError => {
        Future(Ok(Json.obj("failure" -> error.errors.mkString(" "))))
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

  def discuss(id: Long) = Action { implicit request =>
    Ok("")
  }
 }
