package models

import awswrappers.dynamodb._
import models.StatsTable.EmailStats
import org.joda.time.{Duration, DateTime}
import play.api.libs.json.{JsValue, JsObject, Json}
import com.amazonaws.services.dynamodbv2.model._
import java.util.{HashMap => JMap}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


case class JsonDataPoint(name: String, data: List[DateTimePoint])
case class DateTimePoint(timestamp: String, count: Int)

object DateTimePoint {
  implicit val JsonDateTimePointWrites = Json.writes[DateTimePoint]
}
object JsonDataPoint {
  implicit val JsonDataPointWrites = Json.writes[JsonDataPoint]
}

case class EmailStatsSeriesData(
   existingUndeliverables: List[DateTimePoint],
   existingUnsubscribes: List[DateTimePoint],
   hardBounces: List[DateTimePoint],
   softBounces: List[DateTimePoint],
   otherBounces: List[DateTimePoint],
   uniqueClicks: List[DateTimePoint],
   uniqueOpens: List[DateTimePoint],
   numberSent: List[DateTimePoint],
   numberDelivered: List[DateTimePoint],
   unsubscribes: List[DateTimePoint],
   openRate: Double = 0,
   clickthroughRate: Double = 0
   ) {

  def ++ (b: EmailStatsSeriesData): EmailStatsSeriesData = {
    this.copy(
      this.existingUndeliverables ++ b.existingUndeliverables,
      this.existingUnsubscribes ++ b.existingUnsubscribes,
      this.hardBounces ++ b.hardBounces,
      this.softBounces ++ b.softBounces,
      this.otherBounces ++ b.otherBounces,
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
      filterByDay(this.uniqueClicks),
      filterByDay(this.uniqueOpens),
      filterByDay(this.numberSent),
      filterByDay(this.numberDelivered),
      filterByDay(this.unsubscribes)
    )
  }

  def filterByDay(items: List[DateTimePoint]): List[DateTimePoint] = {
    //work out total duration of time series
    val listOpt = for {
      start <- items.headOption
      end <- items.lastOption
    } yield {
      val startDate = new DateTime(start.timestamp)
      val endDate = new DateTime(end.timestamp)
      val days = new Duration(startDate, endDate).getStandardDays.toInt
      //filter so we only have one item per day
      (0 to days).map { day =>
        val newItems = items.filter(item => new DateTime(item.timestamp).toLocalDate == startDate.plusDays(day).toLocalDate)
        //sometimes there are no entry for a day, so just don't report a value for that day
        val itemRet = newItems.lastOption.orElse(None)
        itemRet
      }.toList.flatten
    }
    listOpt.getOrElse(List.empty)
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
  def round(n: Float): Double = {
    BigDecimal(n).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
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
      "openRate" -> a.openRate
    )
  }

  def fromStats(stats: EmailStats) = {
    EmailStatsSeriesData(
      List(DateTimePoint(stats.dateTime, stats.existingUndeliverables)),
      List(DateTimePoint(stats.dateTime, stats.existingUnsubscribes)),
      List(DateTimePoint(stats.dateTime, stats.hardBounces)),
      List(DateTimePoint(stats.dateTime, stats.softBounces)),
      List(DateTimePoint(stats.dateTime, stats.otherBounces)),
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
    List.empty[DateTimePoint])
}
object StatsTable {

  object EmailSendItem {

    implicit val jsonWritesEmailInfo = Json.writes[EmailInfo]
    implicit val jsonReadsEmailInfo = Json.reads[EmailInfo]

    implicit val jsonWritesEmailStats = Json.writes[EmailStats]
    implicit val jsonReadsEmailStats = Json.reads[EmailStats]

    implicit val jsonWrites = Json.writes[EmailSendItem]
    implicit val jsonReads = Json.reads[EmailSendItem]

    def fromAttributeValueMap(xs: Map[String, AttributeValue]) = {
      println("try")
      println(xs)
      for {
        listId <- xs.getString("listId")
        dateTime <- xs.getString("dateTime")
        sendDate <- xs.getString("SendDate")
        existingUndeliverables <- xs.getInt("ExistingUndeliverables")
        existingUnsubscribes <- xs.getInt("ExistingUnsubscribes")
        hardBounces <- xs.getInt("HardBounces")
        softBounces <- xs.getInt("SoftBounces")
        otherBounces <- xs.getInt("OtherBounces")
        uniqueClicks <- xs.getInt("UniqueClicks")
        uniqueOpens <- xs.getInt("UniqueOpens")
        numberSent <- xs.getInt("NumberSent")
        numberDelivered <- xs.getInt("NumberDelivered")
        unsubscribes <- xs.getInt("Unsubscribes")
      } yield {
        println("and pass !")
          EmailSendItem(
            listId,
            dateTime,
            sendDate,
            EmailStats(
              dateTime,
              existingUndeliverables,
              existingUnsubscribes,
              hardBounces,
              softBounces,
              otherBounces,
              uniqueClicks,
              uniqueOpens,
              numberSent,
              numberDelivered,
              unsubscribes)
          )
      }
    }
  }

  case class EmailSendItem(
      listID: String,
      dateTime: String,
      sendDate: String,
      emailStats: EmailStats
                            )
  case class EmailStats(
     dateTime: String,
     existingUndeliverables: Int,
     existingUnsubscribes: Int,
     hardBounces: Int,
     softBounces: Int,
     otherBounces: Int,
     uniqueClicks: Int,
     uniqueOpens: Int,
     numberSent: Int,
     numberDelivered: Int,
     unsubscribes: Int
                         )
  case class EmailInfo(
      dateTime: String,
      missingAddresses: Int,
      subject: String,
      previewURL: String,
      sentDate: String,
      emailName: String,
      status: String,
      isMultipart: Boolean,
      isAlwaysOn: Boolean,
      numberTargeted: Int,
      numberErrored: Int,
      numberExcluded: Int,
      additional: String
      )

  val TableName = "email-send-report-TEST"

  def query(id: Int, startDate: DateTime, endDate: DateTime): Future[Seq[EmailSendItem]]  = {
    def iter(lastEvaluatedKey: Option[java.util.Map[String, AttributeValue]] = None): Future[Seq[StatsTable.EmailSendItem]] = {
      val queryRequest = new QueryRequest()
        .withTableName(TableName)
        .withKeyConditionExpression("listId = :v_id AND #dateTime BETWEEN :v_startdate AND :v_enddate")
        .withExpressionAttributeValues(Map(
          ":v_id" -> new AttributeValue(id.toString),
          ":v_startdate" -> new AttributeValue(startDate.toDateTimeISO.toString),
          ":v_enddate" -> new AttributeValue(endDate.toDateTimeISO.toString)
        ).asJava)
        .withProjectionExpression(
          """listId,
            |#dateTime,
            |SendDate,
            |ExistingUndeliverables,
            |ExistingUnsubscribes,
            |HardBounces,
            |SoftBounces,
            |OtherBounces,
            |ForwardedEmails,
            |UniqueClicks,
            |UniqueOpens,
            |NumberSent,
            |NumberDelivered,
            |Unsubscribes""".stripMargin)
        .withExpressionAttributeNames(Map("#dateTime" -> "dateTime").asJava)
        .withExclusiveStartKey(lastEvaluatedKey.orNull)

      dynamoDbClient.queryFuture(queryRequest) flatMap { result =>
        val theseItems = result.getItems.asScala.toSeq.flatMap { item =>
          EmailSendItem.fromAttributeValueMap(item.asScala.toMap)
        }

        Option(result.getLastEvaluatedKey) match {
          case Some(nextKey) =>
            iter(Some(nextKey)) map { otherItems =>
              theseItems ++ otherItems
            }
          case None =>
            Future.successful(theseItems)
        }
      }
    }

    iter().map(_.sortBy(x => x.dateTime))

  }
}



