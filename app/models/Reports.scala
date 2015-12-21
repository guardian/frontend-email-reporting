package models

import models.StatsTable.{EmailStats, EmailSendItem}
import models.StatsTable.EmailSendItem.jsonWritesEmailStats
import play.api.libs.json._

object Reports {

  val emailLists = Map[String, Int](
    "guardianTodayUk_all" -> 111,
    "guardianTodayUk_new" -> 16216,
    "guardianTodayUs" -> 16125,
    "guardianTodayAu" -> 2014
  )

  def buildBasicStats(data: Seq[EmailSendItem]): JsValue = {
    EmailStatsSeriesData.toJson(mergeStats(data).groupByDay)
  }

  def mergeStats(data: Seq[EmailSendItem]): EmailStatsSeriesData = {
    EmailStatsSeriesData.fromTimeSeries(data.map(_.emailStats))
  }
}
