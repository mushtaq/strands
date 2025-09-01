package strands.rpc.examples.actor

import strands.rpc.examples.actor.{BankAccount, BankAccountApi}
import strands.rpc.*
import sttp.client4.*
import sttp.client4.testing.BackendStub
import sttp.monad.IdentityMonad
import sttp.shared.Identity
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub4.TapirStubInterpreter
import upickle.default.*
import utest.*

object ActorRpcTest extends TestSuite:

  val serverEndpoints: List[ServerEndpoint[Any, Identity]] =
    Service.simpleEndpoints(BankAccount())

  val backendStub: Backend[Identity] =
    TapirStubInterpreter(BackendStub(IdentityMonad))
      .whenServerEndpointsRunLogic(serverEndpoints)
      .backend()

  val bankAccount: Client[BankAccountApi] = Client.from[BankAccountApi](backend = backendStub)

  val tests = Tests:
    test("deposit"):
      val request = basicRequest
        .post(uri"/deposit")
        .body(write(100))
        .response(asJson[Unit])

      println(request.toCurl)
      val response = request.send(backendStub)

      assert(response.body == ())

    test("deposit2"):
      assert(bankAccount.deposit(100) == ())

    test("get balance"):
      val request = basicRequest
        .post(uri"/getBalance")
        .body(write(ujson.Obj()))
        .response(asJson[Int])

      println(request.toCurl)
      val response = request.send(backendStub)

      assert(response.body == 200)

    test("get balance2"):
      assert(bankAccount.getBalance() == 200)

