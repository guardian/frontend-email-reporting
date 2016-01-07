package controllers

import lib.TimeFilter
import models.Reports._
import models.{RawStats, Reports, StatsTable}
import play.api.libs.json._
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

  def rawStats() = Action.async {
    val allRawStats: Future[List[RawStats.InsertMetricGraphStructure]] =
      Future.traverse(RawStats.listIdAndNames.keys.toList){listId =>
        RawStats.getRawStatsFor(listId)
          .map(listOfSignupMetrics =>
            RawStats.graphStructureForInsertMetrics(listId, listOfSignupMetrics))}

    allRawStats.map(rawStats => Ok(views.html.rawstats(Json.toJson(rawStats))))}

  def healthcheck = Action { request =>
    Ok("OK")
  }
}
