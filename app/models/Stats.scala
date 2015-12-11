package models

import awswrappers.dynamodb._
import models.StatsTable.{EmailSendItem, EmailStats}
import play.api.libs.json.{Writes, Json}
import com.amazonaws.services.dynamodbv2.model._

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
}

object EmailStatsSeriesData {

  implicit val EmailStatsSeriesWrites = Json.writes[EmailStatsSeriesData]

  def fromTimeSeries(timeSeries: Seq[EmailStats]) = {
    timeSeries.tail.foldLeft(EmailStatsSeriesData.fromStats(timeSeries.head)){ (acc, item) =>
      acc ++ EmailStatsSeriesData.fromStats(item)
    }
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
    val datetime = s"${stats.date}T${stats.time}:00.000Z"

    EmailStatsSeriesData(
      List(DateTimePoint(datetime, stats.existingUndeliverables)),
      List(DateTimePoint(datetime, stats.existingUnsubscribes)),
      List(DateTimePoint(datetime, stats.hardBounces)),
      List(DateTimePoint(datetime, stats.softBounces)),
      List(DateTimePoint(datetime, stats.otherBounces)),
      List(DateTimePoint(datetime, stats.forwardedEmails)),
      List(DateTimePoint(datetime, stats.uniqueClicks)),
      List(DateTimePoint(datetime, stats.uniqueOpens)),
      List(DateTimePoint(datetime, stats.numberSent)),
      List(DateTimePoint(datetime, stats.numberDelivered)),
      List(DateTimePoint(datetime, stats.unsubscribes))
    )
  }
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
      for {
        date <- xs.getString("date")
        time <- xs.getString("time")
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
        isMultipart <- xs.getBoolean("IsMultipart")
        isAlwaysOn <- xs.getBoolean("IsAlwaysOn")
        numberTargeted <- xs.getInt("NumberTargeted")
        numberErrored <- xs.getInt("NumberErrored")
        numberExcluded <- xs.getInt("NumberExcluded")
        additional <- xs.getString("Additional")
      } yield {
        EmailSendItem(
          date,
          time,
          sendDate,
          fromAddress,
          fromName,
          duplicates,
          invalidAddresses,
          EmailStats(
            date,
            time,
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
            date,
            time,
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
    }
  }

  case class EmailSendItem(
      date: String,
      time: String,
      sendDate: String,
      fromAddress: String,
      fromName: String,
      duplicates: Int,
      invalidAddresses: Int,
      emailStats: EmailStats,
      emailInfo: EmailInfo
                            )
  case class EmailStats(
     date: String,
     time: String,
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
      date: String,
      time: String,
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

  def list() = {

    def iter(lastEvaluatedKey: Option[java.util.Map[String, AttributeValue]]): Future[Seq[StatsTable.EmailSendItem]] = {
      val scanRequest = new ScanRequest()
        .withTableName(TableName)
        .withLimit(10)
        .withExclusiveStartKey(lastEvaluatedKey.orNull)

      dynamoDbClient.scanFuture(scanRequest) flatMap { result =>
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
    iter(None).map(_.sortBy(x => x.time))
  }
}



