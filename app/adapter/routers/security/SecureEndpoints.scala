package adapter.routers.security

import adapter.context.UserContext
import adapter.controllers.AuthorizationController
import adapter.json.writes.{JsValueAuthorizationFailed, JsValueError}
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.play.jsonBody
import sttp.tapir.server.PartialServerEndpoint

import javax.inject.Inject
import scala.concurrent.Future

class SecureEndpoints @Inject() (
    authorizationController: AuthorizationController
):

  private val secureEndpointWithBearer: Endpoint[String, Unit, JsValueError, Unit, Any] =
    endpoint
      .securityIn(auth.bearer[String]())
      .errorOut(
        oneOf[JsValueError](
          oneOfVariant(statusCode(StatusCode.Unauthorized).and(jsonBody[JsValueAuthorizationFailed]))
        )
      )

  val authorizationWithBearerEndpoint: PartialServerEndpoint[String, UserContext, Unit, JsValueError, Unit, Any, Future] =
    secureEndpointWithBearer
      .serverSecurityLogic(authorizationController.authorizationWithBearer)