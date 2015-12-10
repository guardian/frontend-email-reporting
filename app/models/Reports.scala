package models

import models.StatsTable.{EmailStats, EmailSendItem}
import models.StatsTable.EmailSendItem.jsonWritesEmailStats
import play.api.libs.json._

object Reports {

  def buildBasicStats(data: Seq[EmailSendItem]): JsValue = {
    EmailStatsSeriesData.toJson(mergeStats(data))
  }

  def mergeStats(data: Seq[EmailSendItem]): EmailStatsSeriesData = {
    EmailStatsSeriesData.fromTimeSeries(data.map(_.emailStats))
  }
}
