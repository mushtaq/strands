package strands.examples.simple

import strands.examples.simple.{SimpleApi, SimpleImpl}
import strands.examples.simple.SimpleModels.{Book, User, books}
import strands.rpc.{Client, Service}
import sttp.client4.*
import sttp.client4.testing.BackendStub
import sttp.monad.IdentityMonad
import sttp.shared.Identity
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub4.TapirStubInterpreter
import upickle.default.*
import utest.*
import strands.rpc.given
import strands.rpc.*

object SimpleRpcTest extends TestSuite:

  val serverEndpoints: List[ServerEndpoint[Any, Identity]] =
    Service.simpleEndpoints(SimpleImpl())

  val backendStub: Backend[Identity] =
    TapirStubInterpreter(BackendStub(IdentityMonad))
      .whenServerEndpointsRunLogic(serverEndpoints)
      .backend()

  val simpleClient: Client[SimpleApi] = Client.from[SimpleApi](backend = backendStub)

  val tests = Tests:
    test("return hello message"):
      val request = basicRequest
        .post(uri"/hello")
        .body(write(User("Mushtaq")))
        .response(asJson[String])

      println(request.toCurl)
      val response = request.send(backendStub)

      assert(response.body == "Hello Mushtaq")

    test("return hello message2"):
      assert(simpleClient.hello(User("Mushtaq")) == "Hello Mushtaq")

    test("list available books"):
      val request = basicRequest
        .post(uri"/booksListing")
        .response(asJson[List[Book]])

      println(request.toCurl)
      val response = request.send(backendStub)

      assert(response.body == books)

    test("list available books2"):
      assert(simpleClient.booksListing() == books)
