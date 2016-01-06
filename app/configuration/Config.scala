package configuration

import com.gu.googleauth.GoogleAuthConfig
import com.typesafe.config.ConfigFactory
import play.api.Logger

object Config {
  val logger = Logger(this.getClass())

  val config = ConfigFactory.load()

  val GuardianGoogleAppsDomain = "guardian.co.uk"

  val googleAuthConfig = {
    val con = ConfigFactory.load().getConfig("google.oauth")
    GoogleAuthConfig(
      con.getString("client.id"),
      con.getString("client.secret"),
      con.getString("callback"),
      Some(GuardianGoogleAppsDomain) // Google App domain to restrict login
    )
  }

  val staffAuthorisedEmailGroups = config.getString("staff.authorised.emails.groups").split(",").map(group => s"$group@$GuardianGoogleAppsDomain").toSet

}