package models

import org.joda.time.{Duration, DateTime}
import play.api.libs.json.{JsValue, Json}
import models.DateTimePoint
import models.StatsTable._
import models.Utils.round

case class EmailStatsSeriesData (
                                  existingUndeliverables: List[DateTimePoint],
                                  existingUnsubscribes: List[DateTimePoint],
                                  hardBounces: List[DateTimePoint],
                                  softBounces: List[DateTimePoint],
                                  otherBounces: List[DateTimePoint],
                                  forwardedEmails: List[DateTimePoint],
                                  uniqueClicks: List[DateTimePoint],
                                  uniqueOpens: List[DateTimePoint],
                                  numberSent: List[DateTimePoint],
                                  numberDelivered: List[DateTimePoint],
                                  unsubscribes: List[DateTimePoint],
                                  openRate: Double = 0,
                                  clickthroughRate: Double = 0,
                                  weeklyOpenAvg: Double = 0,
                                  weeklyClickAvg: Double = 0
                                ) {

  def ++ (b: EmailStatsSeriesData): EmailStatsSeriesData = {
    this.copy(
      this.existingUndeliverables ++ b.existingUndeliverables,
      this.existingUnsubscribes ++ b.existingUnsubscribes,
      this.hardBounces ++ b.hardBounces,
      this.softBounces ++ b.softBounces,
      this.otherBounces ++ b.otherBounces,
      this.forwardedEmails ++ b.forwardedEmails,
      this.uniqueClicks ++ b.uniqueClicks,
      this.uniqueOpens ++ b.uniqueOpens,
      this.numberSent ++ b.numberSent,
      this.numberDelivered ++ b.numberDelivered,
      this.unsubscribes ++ b.unsubscribes
    )
  }

  def groupByDay = {
    this.copy(
      filterByDay(this.existingUndeliverables),
      filterByDay(this.existingUnsubscribes),
      filterByDay(this.hardBounces),
      filterByDay(this.softBounces),
      filterByDay(this.otherBounces),
      filterByDay(this.forwardedEmails),
      filterByDay(this.uniqueClicks),
      filterByDay(this.uniqueOpens),
      filterByDay(this.numberSent),
      filterByDay(this.numberDelivered),
      filterByDay(this.unsubscribes)
    )
  }

  def filterByDay(items: List[DateTimePoint]): List[DateTimePoint] = {
    //work out total duration of time series
    val start = new DateTime(items.head.timestamp)
    val end = new DateTime(items.last.timestamp)
    val days = new Duration(start, end).getStandardDays.toInt
    //filter so we only have one item per day
    (0 to days).map { day =>
      val newItems = items.filter(item => new DateTime(item.timestamp).toLocalDate == start.plusDays(day).toLocalDate)
      //sometimes there are no entry for a day, so just don't report a value for that day
      newItems.lastOption.orElse(None)
    }.toList.flatten
  }

  def withClickthroughRate: EmailStatsSeriesData = {
    val clickRateOpt = for {
      clicks <- this.uniqueClicks.lift(2)
      delivered <- this.numberDelivered.lift(2)
    } yield {
      this.copy(
        clickthroughRate = round(clicks.count.toFloat / delivered.count.toFloat * 100)
      )
    }
    clickRateOpt.getOrElse(this)
  }

  def withOpenRate: EmailStatsSeriesData = {
    val openRateOpt = for {
      opens <- this.uniqueOpens.lift(2)
      delivered <- this.numberDelivered.lift(2)
    } yield {
      this.copy(
        openRate = round(opens.count.toFloat / delivered.count.toFloat * 100)
      )
    }
    openRateOpt.getOrElse(this)
  }

  def withWeeklyOpenAverage: EmailStatsSeriesData = {
    val opensAvg = this.uniqueOpens.take(7).map(_.count).sum.toFloat
    val deliverAvg = this.numberDelivered.take(7).map(_.count).sum.toFloat
    this.copy(
      weeklyOpenAvg = round( opensAvg / deliverAvg * 100)
    )
  }

  def withWeeklyClickAverage: EmailStatsSeriesData = {
    val clickAvg = this.uniqueClicks.take(7).map(_.count).sum.toFloat
    val deliverAvg = this.numberDelivered.take(7).map(_.count).sum.toFloat
    this.copy(
      weeklyClickAvg = round( clickAvg / deliverAvg * 100)
    )
  }
}

object EmailStatsSeriesData {

  implicit val EmailStatsSeriesWrites = Json.writes[EmailStatsSeriesData]

  def fromTimeSeries(timeSeries: Seq[EmailStats]) = {
    if (timeSeries.isEmpty) {
      EmailStatsSeriesData.empty
    }
    else if (timeSeries.length > 1) {
      timeSeries.tail.foldLeft(EmailStatsSeriesData.fromStats(timeSeries.head)) { (acc, item) =>
        acc ++ EmailStatsSeriesData.fromStats(item)
      }
    } else { EmailStatsSeriesData.fromStats(timeSeries.head) }
  }

  def toJson(a: EmailStatsSeriesData): JsValue = {
    val emailData = Json.toJson(List(
      JsonDataPoint("existingUndeliverables", a.existingUndeliverables),
      JsonDataPoint("existingUnsubscribes", a.existingUnsubscribes),
      JsonDataPoint("hardBounces", a.hardBounces),
      JsonDataPoint("softBounces", a.softBounces),
      JsonDataPoint("otherBounces", a.otherBounces),
      JsonDataPoint("forwardedEmails", a.forwardedEmails),
      JsonDataPoint("uniqueClicks", a.uniqueClicks),
      JsonDataPoint("uniqueOpens", a.uniqueOpens),
      JsonDataPoint("numberSent", a.numberSent),
      JsonDataPoint("numberDelivered", a.numberDelivered),
      JsonDataPoint("unsubscribes", a.unsubscribes)
    )
    )
    Json.obj(
      "emailData" -> emailData,
      "clickThroughRate" -> a.clickthroughRate,
      "clickThroughAvg" -> a.weeklyClickAvg,
      "clickThroughChange" -> round(a.clickthroughRate - a.weeklyClickAvg),
      "openRate" -> a.openRate,
      "openRateAvg" -> a.weeklyOpenAvg,
      "openRateChange" -> round(a.openRate - a.weeklyOpenAvg)
    )
  }

  def fromStats(stats: EmailStats) = {
    EmailStatsSeriesData(
      List(DateTimePoint(stats.dateTime, stats.existingUndeliverables)),
      List(DateTimePoint(stats.dateTime, stats.existingUnsubscribes)),
      List(DateTimePoint(stats.dateTime, stats.hardBounces)),
      List(DateTimePoint(stats.dateTime, stats.softBounces)),
      List(DateTimePoint(stats.dateTime, stats.otherBounces)),
      List(DateTimePoint(stats.dateTime, stats.forwardedEmails)),
      List(DateTimePoint(stats.dateTime, stats.uniqueClicks)),
      List(DateTimePoint(stats.dateTime, stats.uniqueOpens)),
      List(DateTimePoint(stats.dateTime, stats.numberSent)),
      List(DateTimePoint(stats.dateTime, stats.numberDelivered)),
      List(DateTimePoint(stats.dateTime, stats.unsubscribes))
    )
  }

  def empty = EmailStatsSeriesData(
    List.empty[DateTimePoint],
    List.empty[DateTimePoint],
    List.empty[DateTimePoint],
    List.empty[DateTimePoint],
    List.empty[DateTimePoint],
    List.empty[DateTimePoint],
    List.empty[DateTimePoint],
    List.empty[DateTimePoint],
    List.empty[DateTimePoint],
    List.empty[DateTimePoint],
    List.empty[DateTimePoint])
}
