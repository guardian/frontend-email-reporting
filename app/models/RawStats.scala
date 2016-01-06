package models

import awswrappers.dynamodb._
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, QueryRequest}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class InsertMetric(date: String, hits: Int)

object InsertMetric {
  implicit val signupMetricWrites = Json.writes[InsertMetric]
}

object RawStats {
  val TableName = "email-signup-stats"

  //Aim of class is to convert directly to JSON used by highcharts
  case class InsertMetricGraphStructure(
    name: String,
    visible: Boolean,
    //Inner list are tuples (date, hits)
    data: List[List[Long]])

  object InsertMetricGraphStructure {
    implicit val insertMetricGraphStructureWrites = Json.writes[InsertMetricGraphStructure]
  }

  def getRawStatsFor(listId: Int): Future[List[InsertMetric]] = {
    val queryRequest = new QueryRequest()
      .withTableName(TableName)
      .withKeyConditionExpression("list_id = :listId")
      .withExpressionAttributeValues(
        Map(":listId" -> new AttributeValue().withN(listId.toString)).asJava)

    dynamoDbClient.queryFuture(queryRequest).map{ queryResult =>
      queryResult.getItems.asScala.toList
        .map(_.asScala)
        .flatMap { attributeMap =>
          for {
            hits <- attributeMap.get("hits").map(_.getN)
            dateString <- attributeMap.get("date").map(_.getS)
          } yield InsertMetric(dateString, hits.toInt)}}}

  def graphStructureForInsertMetrics(listId: Int, insertMetrics: List[InsertMetric]): InsertMetricGraphStructure =
    InsertMetricGraphStructure(
      name=Reports.niceNames.getOrElse[String](listId, listId.toString),
      visible=true,
      data=insertMetrics.map{
        insertMetric =>
          val dateAsLong: Long = DateTime.parse(insertMetric.date,
            DateTimeFormat.forPattern("yyyy-MM-dd")).getMillis
          List(dateAsLong, insertMetric.hits.toLong)})
}
