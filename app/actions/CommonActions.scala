package actions

import configuration.Config
import controllers.routes
import play.api.mvc._
import com.gu.googleauth

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CommonActions {

  import CommonActions._
  val noCacheAction = resultModifier(noCache)

  val googleAuthAction = OAuthActions.AuthAction

  val googleAuthenticatedStaffAction = noCacheAction andThen googleAuthAction
}

object CommonActions {
  def resultModifier(f: Result => Result) = new ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = block(request).map(f)
  }

  def noCache(result: Result): Result = result.withHeaders("Cache-Control" -> "no-cache, private", "Pragma" -> "no-cache")
}

trait OAuthActions extends com.gu.googleauth.Actions {
  val authConfig = Config.googleAuthConfig

  val loginTarget = routes.OAuth.loginAction()
}

object OAuthActions extends OAuthActions