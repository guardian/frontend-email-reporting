package models

import awswrappers.dynamodb._
import models.StatsTable.{EmailSendItem, EmailStats}
import play.api.libs.json.{Writes, Json}
import com.amazonaws.services.dynamodbv2.model._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


case class JsonDataPoint(name: String, data: List[Int])

object JsonDataPoint {
  implicit val JsonDataPointWrites = Json.writes[JsonDataPoint]
}

case class EmailStatsSeriesData(
   existingUndeliverables: List[Int],
   existingUnsubscribes: List[Int],
   hardBounces: List[Int],
   softBounces: List[Int],
   otherBounces: List[Int],
   forwardedEmails: List[Int],
   uniqueClicks: List[Int],
   uniqueOpens: List[Int],
   numberSent: List[Int],
   numberDelivered: List[Int],
   unsubscribes: List[Int]
   )

object EmailStatsSeriesData {

  implicit val EmailStatsSeriesWrites = Json.writes[EmailStatsSeriesData]

  def fromTimeSeries(timeSeries: Seq[EmailStats]) = {
    timeSeries.tail.foldLeft(EmailStatsSeriesData.fromStats(timeSeries.head)){ (acc, item) =>
      this.add(acc, EmailStatsSeriesData.fromStats(item))
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
    ))
  }

  def fromStats(stats: EmailStats) = {
    EmailStatsSeriesData(
      List(stats.existingUndeliverables),
      List(stats.existingUnsubscribes),
      List(stats.hardBounces),
      List(stats.softBounces),
      List(stats.otherBounces),
      List(stats.forwardedEmails),
      List(stats.uniqueClicks),
      List(stats.uniqueOpens),
      List(stats.numberSent),
      List(stats.numberDelivered),
      List(stats.unsubscribes)
    )
  }

  def add (a: EmailStatsSeriesData, b: EmailStatsSeriesData): EmailStatsSeriesData = {
    a.copy(
      a.existingUndeliverables ++ b.existingUndeliverables,
      a.existingUnsubscribes ++ b.existingUnsubscribes,
      a.hardBounces ++ b.hardBounces,
      a.softBounces ++ b.softBounces,
      a.otherBounces ++ b.otherBounces,
      a.forwardedEmails ++ b.forwardedEmails,
      a.uniqueClicks ++ b.uniqueClicks,
      a.uniqueOpens ++ b.uniqueOpens,
      a.numberSent ++ b.numberSent,
      a.numberDelivered ++ b.numberDelivered,
      a.unsubscribes ++ b.unsubscribes
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



