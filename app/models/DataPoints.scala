package models

import play.api.libs.json.Json

case class JsonDataPoint(name: String, data: List[DateTimePoint])
case class DateTimePoint(timestamp: String, count: Int)

object DateTimePoint {
  implicit val JsonDateTimePointWrites = Json.writes[DateTimePoint]
}
object JsonDataPoint {
  implicit val JsonDataPointWrites = Json.writes[JsonDataPoint]
}

