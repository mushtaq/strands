package strands.examples.actor

import strands.examples.actor.{BankAccount, BankAccountApi}
import strands.rpc.*
import sttp.client4.*
import sttp.client4.testing.BackendStub
import sttp.monad.IdentityMonad
import sttp.shared.Identity
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub4.TapirStubInterpreter
import upickle.default.*
import utest.*

object BankAccountRpcTest extends TestSuite:
  val tests: Tests =
    Tests:
      test("deposit and get balance"):
        ox.supervised:
          val serverEndpoints: List[ServerEndpoint[Any, Identity]] =
            Service.actorEndpoints(BankAccount())

          val backendStub: Backend[Identity] =
            TapirStubInterpreter(BackendStub(IdentityMonad))
              .whenServerEndpointsRunLogic(serverEndpoints)
              .backend()

          val request = basicRequest
            .post(uri"/deposit")
            .body(write(100))
            .response(asJson[Unit])

          println(request.toCurl)
          val response = request.send(backendStub)

          assert(response.body == ())

          val request2 = basicRequest
            .post(uri"/getBalance")
            .body(write(ujson.Obj()))
            .response(asJson[Int])

          println(request2.toCurl)
          val response2 = request2.send(backendStub)

          assert(response2.body == 100)

      test("deposit and get balance2"):
        ox.supervised:
          val serverEndpoints: List[ServerEndpoint[Any, Identity]] =
            Service.actorEndpoints(BankAccount())

          val backendStub: Backend[Identity] =
            TapirStubInterpreter(BackendStub(IdentityMonad))
              .whenServerEndpointsRunLogic(serverEndpoints)
              .backend()

          val bankAccount: Client[BankAccountApi] = Client.from[BankAccountApi](backend = backendStub)

          assert(bankAccount.deposit(100) == ())
          assert(bankAccount.deposit(100) == ())
          assert(bankAccount.getBalance() == 200)
