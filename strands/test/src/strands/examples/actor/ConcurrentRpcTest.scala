package strands.examples.actor

import ox.*
import strands.examples.actor.{BankAccount, BankAccountApi}
import strands.rpc.*
import sttp.client4.testing.StreamBackendStub
import sttp.monad.IdentityMonad
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub4.TapirStreamStubInterpreter
import upickle.default.*
import utest.*

object ConcurrentRpcTest extends TestSuite:
  val tests = Tests:
    test("race condition"):
      val serverEndpoints: RpcEndpoints =
        Service.simpleEndpoints(BankAccount())

      val backendStub: RpcBackend =
        TapirStreamStubInterpreter(StreamBackendStub(IdentityMonad))
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
        val serverEndpoints: RpcEndpoints =
          Service.actorEndpoints(BankAccount())

        val backendStub: RpcBackend =
          TapirStreamStubInterpreter(StreamBackendStub(IdentityMonad))
            .whenServerEndpointsRunLogic(serverEndpoints)
            .backend()

        val bankAccount: Client[BankAccountApi] = Client.from[BankAccountApi](backend = backendStub)

        par:
          (1 to 10000)
            .map(_ => () => bankAccount.deposit(1))

        assert:
          bankAccount.getBalance() == 10000
