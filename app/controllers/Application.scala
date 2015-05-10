package controllers

import play.api.libs.json.{JsError, JsSuccess, JsPath, Reads}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext


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
      case success: JsSuccess[Discussion] => {
        val value = success.get.name
        Future(Ok(""))
      }
      case error: JsError => {
        Future(Ok(""))
      }
    }
  }


  
 }
