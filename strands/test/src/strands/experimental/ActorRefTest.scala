package strands.experimental

import strands.experimental.{ActorSystem, mapPar}
import utest.*

import scala.util.Random

object ActorRefTest extends TestSuite:
  class TestAccount:
    private var balance: Int = 0

    def deposit(amount: Int): Unit =
      balance += amount

    def getBalance: Int = balance

  val actorSystem = new ActorSystem()

  override def utestAfterAll(): Unit = actorSystem.close()

  val tests =
    Tests:
      val max = 1000
      val range = 1 to max

      test("plain object is not thread safe"):
        val account = new TestAccount()

        range.mapPar: _ =>
          account.deposit(1)

        val balance = account.getBalance

        assert(balance != max)

      test("actor ref is thread safe"):
        val accountRef = actorSystem.spawn(new TestAccount())

        range.mapPar: _ =>
          accountRef.ask(acc => acc.deposit(1))

        val balance = accountRef.ask(acc => acc.getBalance)

        assert(balance == max)

      test("actor ref is thread safe2"):
        val accountRef = actorSystem.spawn:
          new TestAccount()

        range.mapPar: _ =>
          accountRef.ask: acc =>
            Thread.sleep(Random.nextLong(1000))
            acc.deposit(1)

        val balance = accountRef.ask: acc =>
          acc.getBalance

        assert(balance == max)
