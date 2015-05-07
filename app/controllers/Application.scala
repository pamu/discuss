package controllers

import play.api.libs.json.{JsPath, Reads}
import play.api.mvc.{Action, Controller}


object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Discuss"))
  }
  case class Discussion(name: String)
  implicit val discussionReads: Reads[Discussion] = (
    (JsPath \ "name").read[String].map(Discussion(_))
    )

  def discussion() = Action(parse.json) { implicit request =>
    Ok("")
  }

  
 }
