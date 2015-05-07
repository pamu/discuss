package controllers

import play.api.mvc.{Action, Controller}


object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Discuss"))
  }

  def discussion() = Action(parse.json) { implicit request =>
    Ok("")
  }

  
 }
