package strands.examples.actor

import ox.*
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

object ConcurrentRpcTest extends TestSuite:
  val tests = Tests:
    test("race condition"):
      val serverEndpoints: List[ServerEndpoint[Any, Identity]] =
        Service.simpleEndpoints(BankAccount())

      val backendStub: Backend[Identity] =
        TapirStubInterpreter(BackendStub(IdentityMonad))
          .whenServerEndpointsRunLogic(serverEndpoints)
          .backend()

      val bankAccount: Client[BankAccountApi] = Client.from[BankAccountApi](backend = backendStub)

      par:
        (1 to 10000)
          .map(_ => () => bankAccount.deposit(1))

      assert:
        bankAccount.getBalance() != 10000

    test("thread safe"):
      supervised:
        val serverEndpoints: List[ServerEndpoint[Any, Identity]] =
          Service.actorEndpoints(BankAccount())

        val backendStub: Backend[Identity] =
          TapirStubInterpreter(BackendStub(IdentityMonad))
            .whenServerEndpointsRunLogic(serverEndpoints)
            .backend()

        val bankAccount: Client[BankAccountApi] = Client.from[BankAccountApi](backend = backendStub)

        par:
          (1 to 10000)
            .map(_ => () => bankAccount.deposit(1))

        assert:
          bankAccount.getBalance() == 10000
