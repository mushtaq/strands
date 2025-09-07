package strands.examples.actor

import strands.examples.TestHelpers.backendStub
import strands.examples.actor.{BankAccount, BankAccountApi}
import strands.rpc.*
import strands.rpc.common.RpcBackend
import sttp.client4.*
import sttp.client4.upicklejson.default.{asJson, asJsonOrFail}
import upickle.default.*
import utest.*

object BankAccountRpcTest extends TestSuite:
  val tests: Tests =
    Tests:
      test("deposit and get balance"):
        ox.supervised:
          val backendStub: RpcBackend = Service.actorEndpoints(BankAccount()).backendStub()
          val bankAccount: Client[BankAccountApi] = Client.of[BankAccountApi](backendStub)

          assert(bankAccount.deposit(100) == ())
          assert(bankAccount.deposit(100) == ())
          assert(bankAccount.getBalance() == 200)

      test("deposit and get balance2"):
        ox.supervised:
          val backendStub: RpcBackend = Service.actorEndpoints(BankAccount()).backendStub()

          val request = basicRequest
            .post(uri"/deposit")
            .body(asJson(100))
            .response(asJsonOrFail[Unit])

          println(request.toCurl)
          val response = request.send(backendStub)

          assert(response.body == ())

          val request2 = basicRequest
            .post(uri"/getBalance")
            .response(asJsonOrFail[Int])

          println(request2.toCurl)
          val response2 = request2.send(backendStub)

          assert(response2.body == 100)
