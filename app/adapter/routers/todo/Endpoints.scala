package adapter.routers.todo

import adapter.controllers.TodoController
import adapter.json.{reads, writes}
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.play.*
import sttp.tapir.server.ServerEndpoint

import javax.inject.Inject
import scala.concurrent.Future

class Endpoints @Inject() (
    todoController: TodoController
):

  private val findTodoEndpoint: PublicEndpoint[Long, writes.JsValueError, writes.JsValueTodo, Any] =
    endpoint.get
      .in("todo" / path[Long]("id"))
      .out(jsonBody[writes.JsValueTodo])
      .errorOut(jsonBody[writes.JsValueError])

  private val createTodoEndpoint: PublicEndpoint[reads.JsValueTodo, writes.JsValueError, Unit, Any] =
    endpoint.post
      .in("todo")
      .in(jsonBody[reads.JsValueTodo])
      .errorOut(jsonBody[writes.JsValueError])
      .out(statusCode(StatusCode.NoContent))

  private val updateTodoEndpoint: PublicEndpoint[(Long, reads.JsValueTodo), writes.JsValueError, Unit, Any] =
    endpoint.put
      .in("todo" / path[Long]("id"))
      .in(jsonBody[reads.JsValueTodo])
      .errorOut(jsonBody[writes.JsValueError])
      .out(statusCode(StatusCode.NoContent))

  private val deleteTodoEndpoint: PublicEndpoint[Long, writes.JsValueError, Unit, Any] =
    endpoint.delete
      .in("todo" / path[Long]("id"))
      .errorOut(jsonBody[writes.JsValueError])

  val endpoints: List[ServerEndpoint[Any, Future]] = List(
    findTodoEndpoint.serverLogic(id => todoController.get(id)),
    createTodoEndpoint.serverLogic(jsValueTodo => todoController.create(jsValueTodo)),
    updateTodoEndpoint.serverLogic((id, jsValueTodo) => todoController.update(id, jsValueTodo)),
    deleteTodoEndpoint.serverLogicSuccess(id => todoController.delete(id))
  )