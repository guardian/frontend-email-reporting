package models

import awswrappers.dynamodb._
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap
import models.StatsTable.{EmailSendItem, EmailStats}
import org.joda.time.{Interval, ReadableInstant, Duration, DateTime}
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{Writes, Json}
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
   forwardedEmails: List[DateTimePoint],
   uniqueClicks: List[DateTimePoint],
   uniqueOpens: List[DateTimePoint],
   numberSent: List[DateTimePoint],
   numberDelivered: List[DateTimePoint],
   unsubscribes: List[DateTimePoint]
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

  def toJson(a: EmailStatsSeriesData) = {
    Json.toJson(List(
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
object StatsTable {

  object EmailSendItem {

    implicit val jsonWritesEmailInfo = Json.writes[EmailInfo]
    implicit val jsonReadsEmailInfo = Json.reads[EmailInfo]

    implicit val jsonWritesEmailStats = Json.writes[EmailStats]
    implicit val jsonReadsEmailStats = Json.reads[EmailStats]

    implicit val jsonWrites = Json.writes[EmailSendItem]
    implicit val jsonReads = Json.reads[EmailSendItem]

    def fromAttributeValueMap(xs: Map[String, AttributeValue]) = {
      val data = for {
        listId <- xs.getString("listId")
        dateTime <- xs.getString("dateTime")
        sendDate <- xs.getString("SendDate")
        fromAddress <- xs.getString("FromAddress")
        fromName <- xs.getString("FromName")
        duplicates <- xs.getInt("Duplicates")
        invalidAddresses <- xs.getInt("InvalidAddresses")
        existingUndeliverables <- xs.getInt("ExistingUndeliverables")
        existingUnsubscribes <- xs.getInt("ExistingUnsubscribes")
        hardBounces <- xs.getInt("HardBounces")
        softBounces <- xs.getInt("SoftBounces")
        otherBounces <- xs.getInt("OtherBounces")
        forwardedEmails <- xs.getInt("ForwardedEmails")
        uniqueClicks <- xs.getInt("UniqueClicks")
        uniqueOpens <- xs.getInt("UniqueOpens")
        numberSent <- xs.getInt("NumberSent")
        numberDelivered <- xs.getInt("NumberDelivered")
        unsubscribes <- xs.getInt("Unsubscribes")
        missingAddresses <- xs.getInt("MissingAddresses")
        subject <- xs.getString("Subject")
        previewURL <- xs.getString("PreviewURL")
        sentDate <- xs.getString("SentDate")
        emailName <- xs.getString("EmailName")
        status <- xs.getString("Status")
        isMultipart <- xs.getInt("IsMultipart").map(_ == 1)
        isAlwaysOn <- xs.getInt("IsAlwaysOn").map(_ == 1)
        numberTargeted <- xs.getInt("NumberTargeted")
        numberErrored <- xs.getInt("NumberErrored")
        numberExcluded <- xs.getInt("NumberExcluded")
        additional <- xs.getString("Additional")
      } yield {
        EmailSendItem(
          listId,
          dateTime,
          sendDate,
          fromAddress,
          fromName,
          duplicates,
          invalidAddresses,
          EmailStats(
            dateTime,
            existingUndeliverables,
            existingUnsubscribes,
            hardBounces,
            softBounces,
            otherBounces,
            forwardedEmails,
            uniqueClicks,
            uniqueOpens,
            numberSent,
            numberDelivered,
            unsubscribes),
          EmailInfo(
            dateTime,
            missingAddresses,
            subject,
            previewURL,
            sentDate,
            emailName,
            status,
            isMultipart,
            isAlwaysOn,
            numberTargeted,
            numberErrored,
            numberExcluded,
            additional
          )
        )
      }
      println(data)
      data
    }
  }

  case class EmailSendItem(
      listID: String,
      dateTime: String,
      sendDate: String,
      fromAddress: String,
      fromName: String,
      duplicates: Int,
      invalidAddresses: Int,
      emailStats: EmailStats,
      emailInfo: EmailInfo
                            )
  case class EmailStats(
     dateTime: String,
     existingUndeliverables: Int,
     existingUnsubscribes: Int,
     hardBounces: Int,
     softBounces: Int,
     otherBounces: Int,
     forwardedEmails: Int,
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

  def makeItemId(listId: Int, date: Option[DateTime] = None): String = {
    val dateToday = date.getOrElse(new DateTime())
    val fmt = ISODateTimeFormat.date()
    s"${listId}#${fmt.print(dateToday)}"
  }

  def query(id: Int, startDate: DateTime, endDate: DateTime): Future[Seq[EmailSendItem]]  = {
    def iter(lastEvaluatedKey: Option[java.util.Map[String, AttributeValue]]): Future[Seq[StatsTable.EmailSendItem]] = {
      val queryRequest = new QueryRequest()
        .withTableName(TableName)
        .withKeyConditionExpression("listId = :v_id AND #dateTime BETWEEN :v_startdate AND :v_enddate")
        .withExpressionAttributeValues(Map(
          ":v_id" -> new AttributeValue(id.toString),
          ":v_startdate" -> new AttributeValue(startDate.toDateTimeISO.toString),
          ":v_enddate" -> new AttributeValue(endDate.toDateTimeISO.toString)
        ).asJava)
        .withExpressionAttributeNames(Map("#dateTime" -> "dateTime").asJava)

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
    val query = Map("listId" -> new AttributeValue(makeItemId(id))).asJava

    iter(Some(query)).map(_.sortBy(x => x.dateTime))

  }
}



