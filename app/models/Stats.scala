package models

import awswrappers.dynamodb._
import play.api.libs.json.Json
import com.amazonaws.services.dynamodbv2.model._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object StatsTable {

  object EmailSendItem {

    implicit val jsonWritesEmailInfo = Json.writes[EmailInfo]
    implicit val jsonReadsEmailInfo = Json.reads[EmailInfo]

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
          unsubscribes,
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
      unsubscribes: Int,
      emailInfo: EmailInfo
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



