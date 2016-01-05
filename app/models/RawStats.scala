package models

import awswrappers.dynamodb._
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, QueryRequest}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class SignupMetric(hits: Int, date: String)

object SignupMetric {
  implicit val signupMetricWrites = Json.writes[SignupMetric]
}

object RawStats {

  val TableName = "email-signup-stats"

  def getRawStatsFor(listId: Int): Future[List[SignupMetric]] = {
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
          } yield SignupMetric(hits.toInt, date)}}}
}
