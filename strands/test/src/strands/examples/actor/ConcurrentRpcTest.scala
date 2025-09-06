package strands.examples.actor

import ox.*
import strands.examples.TestHelpers.backendStub
import strands.examples.actor.{BankAccount, BankAccountApi}
import strands.rpc.*
import sttp.tapir.server.ServerEndpoint
import upickle.default.*
import utest.*

object ConcurrentRpcTest extends TestSuite:
  val tests = Tests:
    test("race condition"):
      val backendStub = Service.simpleEndpoints(BankAccount()).backendStub()
      val bankAccount: Client[BankAccountApi] = Client.from[BankAccountApi](backend = backendStub)

      par:
        (1 to 10000)
          .map(_ => () => bankAccount.deposit(1))

      assert:
        bankAccount.getBalance() != 10000

    test("thread safe"):
      supervised:
        val backendStub = Service.actorEndpoints(BankAccount()).backendStub()
        val bankAccount: Client[BankAccountApi] = Client.from[BankAccountApi](backend = backendStub)

        par:
          (1 to 10000)
            .map(_ => () => bankAccount.deposit(1))

        assert:
          bankAccount.getBalance() == 10000
