package strands.rpc.examples.actor

import strands.rpc.{Rpc, Service}
import ox.*
import strands.rpc.Client
import sttp.model.Uri.UriContext

object ActorRpcServer extends OxApp.Simple:

  def run(using Ox): Unit =
    Rpc.startServer(
      Service.actorEndpoints(BankAccount()),
      port = 8081,
      name = "actor-example",
      version = "0.1.0"
    )
    never

object ActorRpcClient extends OxApp.Simple:

  def run(using Ox): Unit =
    val bankAccount: Client[BankAccountApi] = Client.from(baseUri = Some(uri"http://localhost:8081"))

    println("******* Current Balance: " + bankAccount.getBalance())

    println("******* Depositing 1000 in parallel")
    par:
      (1 to 1000)
        .map(_ => () => bankAccount.deposit(1))

    println("******* Current Balance: " + bankAccount.getBalance())
