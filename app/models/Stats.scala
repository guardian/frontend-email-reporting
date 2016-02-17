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
import models.DateTimePoint
import models.EmailStatsSeriesData

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



