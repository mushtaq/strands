package strands.rpc.examples.actor

import ox.*
import strands.rpc.{Rpc, Service}

object ActorRpcServer extends OxApp.Simple:

  def run(using Ox): Unit =
    Rpc.startServer(
      Service.actorEndpoints(BankAccount()),
      8081,
      "actor-example",
      "0.1.0"
    )
    never
