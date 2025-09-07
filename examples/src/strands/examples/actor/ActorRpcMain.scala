package strands.examples.actor

import strands.rpc.{RpcServer, Service}
import ox.*
import strands.rpc.Client
import sttp.model.Uri.UriContext

object ActorRpcServer extends OxApp.Simple:

  def run(using Ox): Unit =
    RpcServer.start(
      Service.actorEndpoints(BankAccount()),
      port = 8081,
      name = "actor-example",
      version = "0.1.0"
    )
    never

object ActorRpcClient extends OxApp.Simple:

  def run(using Ox): Unit =
    val bankAccount: Client[BankAccountApi] = Client.of(uri"http://localhost:8081")

    println("******* Current Balance: " + bankAccount.getBalance())

    println("******* Depositing 1000 in parallel")
    par:
      (1 to 1000)
        .map(_ => () => bankAccount.deposit(1))

    println("******* Current Balance: " + bankAccount.getBalance())
