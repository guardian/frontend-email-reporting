package controllers

import lib.TimeFilter
import models.Reports._
import models.{Reports, StatsTable}
import org.joda.time.DateTime
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application extends Controller {

  def index(times: TimeFilter) = Action {
    val from = times.from.getMillis.toString
    val to = times.to.getMillis.toString

    Ok(views.html.index("The Guardian Daily Email", Reports.emailSendDefinition, from, to))
  }

  def stats(id: Int, times: TimeFilter) = Action.async { request =>
    StatsTable.query(id, times.from, times.to).map { stats =>
      Ok(buildBasicStats(stats))
    }
  }

  def healthcheck = Action { request =>
    Ok("OK")
  }
}
