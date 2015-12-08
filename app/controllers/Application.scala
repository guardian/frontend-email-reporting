package controllers

import models.StatsTable
import models.StatsTable.EmailSendItem._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("The Guardian Daily Email"))
  }

  def stats = Action.async { request =>
    StatsTable.list().map { data =>
      val result = JsArray(data.toList.map(item => Json.toJson(item)))
      Ok(result)
    }
  }
}
