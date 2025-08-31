package strands.rpc.examples.actor

import ox.*
import strands.rpc.{Rpc, Service}
import sttp.tapir.server.ServerEndpoint

object ActorRpcMain extends OxApp.Simple:

  def run(using Ox): Unit =
    Rpc.startServer(
      Service.simpleEndpoints(BankAccount()),
      8081,
      "actor-example",
      "0.1.0"
    )
    never
