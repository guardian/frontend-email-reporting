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

  def buildBasicStats(data: Seq[EmailSendItem]): JsValue = {
    EmailStatsSeriesData.toJson(mergeStats(data))
  }

  def mergeStats(data: Seq[EmailSendItem]): EmailStatsSeriesData = {
    EmailStatsSeriesData.fromTimeSeries(data.map(_.emailStats))
  }
}
