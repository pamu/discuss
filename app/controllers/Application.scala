package controllers

import actors.Client.Result
import actors.Counters.{Discussions, Comments, CounterResult}
import actors.DataStore.Entry
import actors.{Counters, DataStore}
import global.Global
import play.api.libs.json.{JsError, JsSuccess, JsPath, Reads}
import play.api.mvc.{Action, Controller}

import akka.pattern.ask

import scala.concurrent.Future


object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Discuss"))
  }
  case class Discussion(name: String)
  implicit val discussionReads: Reads[Discussion] = (
    (JsPath \ "name").read[String].map(Discussion(_))
    )

  def discussion() = Action.async(parse.json) { implicit request =>
    request.body.validate[Discussion] match {
      case success: JsSuccess[Discussion] =>
        val value = success.get.name
        val f: Future[CounterResult] = (Global.counter ? Counters.Discussions).mapTo[CounterResult]

        f match {
          case Discussions(count) =>

          case Comments(count) =>
        }
        //Global.dataStore ? DataStore.Entry
        Future(Ok(""))
      case error: JsError =>
        Future(Ok(""))
    }
  }


  
 }
