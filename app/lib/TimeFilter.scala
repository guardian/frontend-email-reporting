package lib

import org.joda.time.DateTime
import play.api.mvc.QueryStringBindable

case class TimeFilter(from: DateTime, to: DateTime )

object TimeFilter {
  implicit def queryStringBinder(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[TimeFilter] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String,TimeFilter]] = {

      def optionalParam(paramName: String): Option[String] =
        stringBinder.bind(paramName, params).flatMap(_.right.toOption)

      def asDateTime(s: String) = new DateTime(s.toLong)

      val from = optionalParam("fromDate")
        .map(asDateTime).getOrElse(DateTime.now.minusDays(7))

      val to = optionalParam("toDate")
        .map(asDateTime).getOrElse(DateTime.now)

      Some(Right(TimeFilter(from, to)))
    }
    override def unbind(key: String, value: TimeFilter): String = {
      s"${value.from} ${value.to}"
    }
  }
}
