package models

import models.StatsTable.{EmailStats, EmailSendItem}
import models.StatsTable.EmailSendItem.jsonWritesEmailStats
import play.api.libs.json._

object Reports {

  val emailSendDefinition = Map[String, Int](
    "Guardian Today Uk all" -> 111,
    "Guardian Today Uk new" -> 16216,
    "Guardian Today Us" -> 16125,
    "Guardian Today Au" -> 2014)

  def buildBasicStats(data: Seq[EmailSendItem]): JsValue = {
    val stats = mergeStats(data)
      .groupByDay
      .withClickthroughRate
      .withOpenRate

    EmailStatsSeriesData.toJson(stats)
  }

  def mergeStats(data: Seq[EmailSendItem]): EmailStatsSeriesData = {
    EmailStatsSeriesData.fromTimeSeries(data.map(_.emailStats))
  }
}
