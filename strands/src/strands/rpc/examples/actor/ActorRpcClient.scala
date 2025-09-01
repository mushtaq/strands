package strands.rpc.examples.actor

import ox.*
import strands.rpc.Client
import sttp.model.Uri.UriContext

object ActorRpcClient extends OxApp.Simple:

  def run(using Ox): Unit =
    val bankAccount: Client[BankAccountApi] =
      Client.from(baseUri = Some(uri"http://localhost:8081"))

    println("******* Current Balance: ")
    println(bankAccount.getBalance())

    par:
      (1 to 1000)
        .map(_ => () => bankAccount.deposit(1))

    println("******* Current Balance: ")
    println(bankAccount.getBalance())
