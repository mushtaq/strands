package strands.examples.simple

import strands.examples.TestHelpers.backendStub
import strands.examples.simple.SimpleModels.{Book, Timestamp, User, books}
import strands.examples.simple.{SimpleApi, SimpleImpl}
import strands.rpc.{*, given}
import sttp.client4.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.sync.OxStreams
import upickle.default.*
import utest.*

import scala.concurrent.duration.DurationInt

object SimpleRpcTest extends TestSuite:
  val backendStub: RpcBackend = Service.simpleEndpoints(SimpleImpl()).backendStub()
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

    test("sse"):
      val request = basicRequest
        .get(uri"/ticks")
        .body(write(100.millis))
        .response(asStreamAlwaysUnsafe(OxStreams))

      println(request.toCurl)
      val response = request.send(backendStub)

      val xs = response.body.asSseOf[Timestamp].take(3)
      xs.runForeach(println)
      assert(xs.runToList().size == 3)

    test("sse2"):
      val xs = simpleClient.ticks(100.millis).take(3)
      xs.runForeach(println)
      assert(xs.runToList().size == 3)
