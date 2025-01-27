package adapter.routers

import adapter.json.writes.JsValueInternalServerError
import adapter.routers.todo.Endpoints
import org.apache.pekko.stream.Materializer
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import sttp.tapir.json.play.jsonBody
import sttp.tapir.server.interceptor.exception.ExceptionHandler
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.server.play.{PlayServerInterpreter, PlayServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApiRouter @Inject() (
    todoEndpoints: Endpoints
)(using
    Materializer,
    ExecutionContext
) extends SimpleRouter:
  override def routes: Routes =
    interpreter
      .toRoutes(
        swaggerEndpoints
          ++ todoEndpoints.endpoints
      )

  private val exceptionHandler: ExceptionHandler[Future] =
    ExceptionHandler.pure[Future] { ctx =>
      Some(
        ValuedEndpointOutput[JsValueInternalServerError](jsonBody[JsValueInternalServerError], JsValueInternalServerError(s"Internal Server Error: ${ctx.e}"))
      )
    }

  private val commonPlayServerOption: PlayServerOptions = PlayServerOptions
    .customiseInterceptors()
    .exceptionHandler(exceptionHandler)
    .options

  private val interpreter = PlayServerInterpreter(commonPlayServerOption)

  private val swaggerEndpoints = SwaggerInterpreter().fromServerEndpoints[Future](todoEndpoints.endpoints, "play-tapir-sandbox", "1.0")
