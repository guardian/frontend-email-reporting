package controllers

import lib.TimeFilter
import models.Reports._
import models.{RawStats, Reports, StatsTable}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application extends Controller {

  def index(times: TimeFilter) = Action {
    val from = times.from.getMillis.toString
    val to = times.to.getMillis.toString

    Ok(views.html.index("The Guardian Daily Email", Reports.lists, from, to))
  }

  def stats(id: Int, times: TimeFilter) = Action.async { request =>
    StatsTable.query(id, times.from, times.to).map { stats =>
      Ok(buildBasicStats(stats))
    }
  }

  def rawStats() = Action.async {
    val allRawStats: Future[JsArray] =
      Future.traverse(Reports.lists.values){listId =>
        RawStats.getRawStatsFor(listId)
          .map(listOfSignupMetrics =>
            Json.obj(
              "name" -> Reports.niceNames.getOrElse[String](listId, listId.toString),
              "visible" -> true,
              "data" -> listOfSignupMetrics.map{ metric =>
                val dateAsLong: Long = DateTime.parse(metric.date,
                  DateTimeFormat.forPattern("yyyy-MM-dd")).getMillis

                JsArray(List(JsNumber(dateAsLong), JsNumber(metric.hits)))}
            ))}
      .map(_.toList)
      .map(JsArray(_))

    allRawStats.map(rawStats => Ok(views.html.rawstats(rawStats)))
  }
}
