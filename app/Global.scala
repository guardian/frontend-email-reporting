
import actions.CommonActions
import play.api.GlobalSettings
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.api.routing.Router.Routes

import scala.concurrent.Future

object AuthorizedFilter {
  def apply(actionNames: String*) = new AuthorizedFilter(actionNames)
}

class AuthorizedFilter(actionNames: Seq[String]) extends Filter with CommonActions {

  def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    if(authorizationNotRequired(request)) {
      next(request)
    }
    else {
      val action = googleAuthenticatedStaffAction.async(req => next(request))
      action.apply(Request[AnyContent](request, AnyContentAsEmpty))
    }
  }

  private def authorizationNotRequired(request: RequestHeader) = {
    val actionInvoked: String = request.tags.getOrElse(play.api.routing.Router.Tags.RouteActionMethod, "")
    actionNames.contains(actionInvoked)
  }
}

object HTTPSRedirectFilter extends Filter {

  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    //play uses lower case headers.
    requestHeader.headers.get("x-forwarded-proto") match {
      case Some(header) => {
        if ("https" == header) {
          nextFilter(requestHeader).map { result =>
            result.withHeaders(("Strict-Transport-Security", "max-age=31536000"))
          }
        } else {
          Future.successful(Results.Redirect("https://" + requestHeader.host + requestHeader.uri, 301))
        }
      }
      case None => nextFilter(requestHeader)
    }
  }
}

object Global extends WithFilters(HTTPSRedirectFilter, AuthorizedFilter("login", "loginAction", "oauth2Callback", "healthcheck")) with GlobalSettings {

  override def onStart(application: play.api.Application): Unit = {}

  override def onStop(application: play.api.Application) {}

}