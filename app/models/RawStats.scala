package models

import awswrappers.dynamodb._
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, QueryRequest}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class InsertMetric(hits: Int, date: String)

object InsertMetric {
  implicit val signupMetricWrites = Json.writes[InsertMetric]
}

object RawStats {

  val TableName = "email-signup-stats"

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
            date <- attributeMap.get("date").map(_.getS)
          } yield InsertMetric(hits.toInt, date)}}}
}
