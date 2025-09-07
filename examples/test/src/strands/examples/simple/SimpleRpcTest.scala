package strands.examples.simple

import strands.examples.TestHelpers.backendStub
import strands.examples.simple.SimpleModels.{Book, Timestamp, User, books}
import strands.examples.simple.{SimpleApi, SimpleImpl}
import strands.rpc.{Client, Service}
import strands.rpc.common.RpcHelpers.{asSseOf, given}
import strands.rpc.common.{RpcBackend, RpcHelpers}
import sttp.client4.*
import sttp.client4.upicklejson.default.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.sync.OxStreams
import utest.*
import sttp.tapir.generic.auto.*
import upickle.implicits.namedTuples.default.given

import scala.concurrent.duration.DurationInt

object SimpleRpcTest extends TestSuite:
  val backendStub: RpcBackend = Service.simpleEndpoints(SimpleImpl()).backendStub()
  val simpleClient: Client[SimpleApi] = Client.of[SimpleApi](backendStub)

  val tests = Tests:
    test("return hello message with client"):
      assert(simpleClient.hello(User("Mushtaq")) == "Hello Mushtaq")

    test("return hello message"):
      val request = basicRequest
        .post(uri"/hello")
        .body(asJson(User("Mushtaq")))
        .response(asJsonOrFail[String])

      println(request.toCurl)
      val response = request.send(backendStub)

      assert(response.body == "Hello Mushtaq")

    test("list available books with client"):
      assert(simpleClient.booksListing() == books)

    test("list available books"):
      val request = basicRequest
        .post(uri"/booksListing")
        .response(asJsonOrFail[List[Book]])

      println(request.toCurl)
      val response = request.send(backendStub)

      assert(response.body == books)

    test("ticks with client"):
      val xs = simpleClient.ticks(100.millis).take(3)
      xs.runForeach(println)
      assert(xs.runToList().size == 3)

    test("ticks"):
      val request = basicRequest
        .get(uri"/ticks")
        .body(asJson(100.millis))
        .response(asStreamAlwaysUnsafe(OxStreams))

      println(request.toCurl)
      val response = request.send(backendStub)

      val xs = response.body.asSseOf[Timestamp].take(3)
      xs.runForeach(println)
      assert(xs.runToList().size == 3)

    test("ticks2 with client"):
      val xs = simpleClient.ticks2().take(3)
      xs.runForeach(println)
      assert(xs.runToList().size == 3)

    test("ticks2"):
      val request = basicRequest
        .get(uri"/ticks2")
        .response(asStreamAlwaysUnsafe(OxStreams))

      println(request.toCurl)
      val response = request.send(backendStub)

      val xs = response.body.asSseOf[Timestamp].take(3)
      xs.runForeach(println)
      assert(xs.runToList().size == 3)

    test("join with client"):
      assert(simpleClient.join(first = "Mushtaq", second = "Ahmed") == "Mushtaq Ahmed")

    test("join"):
      val request = basicRequest
        .post(uri"/join")
        .body(asJson((first = "Mushtaq", second = "Ahmed")))
        .response(asJsonOrFail[String])

      println(request.toCurl)
      val response = request.send(backendStub)

      assert(response.body == "Mushtaq Ahmed")
