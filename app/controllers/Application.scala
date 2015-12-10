package controllers

import models.Reports._
import models.StatsTable
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("The Guardian Daily Email"))
  }

  def stats = Action.async { request =>
    StatsTable.list().map { stats =>
      Ok(buildBasicStats(stats))
    }
  }
}
