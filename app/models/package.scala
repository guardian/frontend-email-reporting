import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import play.api.Logger
import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}

import scala.util.Try

package object models {
  val dynamoDbClient: AmazonDynamoDBAsyncClient = if (play.api.Play.isDev(play.api.Play.current)) {
    new AmazonDynamoDBAsyncClient(new ProfileCredentialsProvider("frontend")).withRegion(Regions.EU_WEST_1)
  } else {
    new AmazonDynamoDBAsyncClient().withRegion(Regions.EU_WEST_1)
  }


  implicit class RichAttributeMap(map: Map[String, AttributeValue]) {
    def getString(k: String): Option[String] =
      map.get(k).flatMap(v => Option(v.getS))

    def getLong(k: String): Option[Long] = {
      map.get(k).flatMap({ v =>
        val x = Try(v.getN.toLong)

        x.failed foreach {
          error => Logger.error(s"Error de-serializing $k as Long", error)
        }
        println("moot", x)
        x.toOption
      })
    }

    def getInt(k: String): Option[Int] = {
      map.get(k).flatMap({ v =>
        val x = Try(v.getN.toInt)

        x.failed foreach {
          error => Logger.error(s"Error de-serializing $k as Int", error)
        }
        println("woot", x)
        x.toOption
      })
    }

    def getBoolean(k: String): Option[Boolean] =
      map.get(k).flatMap(v => Option(v.getBOOL))

    def getDateTime(k: String): Option[DateTime] =
      getLong(k).map(n => new DateTime(n))

    def getJson(k: String) = getString(k).flatMap(s => Try(Json.parse(s)).toOption)

    def getSerializedJson[A](k: String)(implicit formatter: Format[A]) =
      getJson(k).flatMap(x => formatter.reads(x).asOpt)
  }
}