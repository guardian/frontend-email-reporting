package models

import models.StatsTable.{EmailStats, EmailSendItem}
import models.StatsTable.EmailSendItem.jsonWritesEmailStats
import play.api.libs.json._

object Reports {

  val lists = Map[String, Int](
    "guardianTodayUk" -> 37,
    "guardianTodayUs" -> 1493,
    "guardianTodayAu" -> 1506
  )

  val niceNames = Map[Int, String](
    37 -> "UK Today",
    1493 -> "US Today",
    1506 ->"AU Today")

  def buildBasicStats(data: Seq[EmailSendItem]): JsValue = {
    EmailStatsSeriesData.toJson(mergeStats(data))
  }

  def mergeStats(data: Seq[EmailSendItem]): EmailStatsSeriesData = {
    EmailStatsSeriesData.fromTimeSeries(data.map(_.emailStats))
  }
}
