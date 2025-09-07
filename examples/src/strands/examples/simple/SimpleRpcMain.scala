package strands.examples.simple

import ox.*
import strands.examples.simple.SimpleModels.User
import strands.rpc.{Client, Rpc, RpcEndpoints, Service}
import sttp.model.Uri.UriContext
import strands.rpc.RpcHelpers.given

object SimpleRpcMain extends OxApp.Simple:

  def run(using Ox): Unit =
    val serverEndpoints: RpcEndpoints =
      Service.simpleEndpoints(SimpleImpl())

    Rpc.startServer(serverEndpoints, 8080, "simple-example", "0.1.0")

    println()
    
    val simpleClient: Client[SimpleApi] =
      Client.of[SimpleApi](uri"http://localhost:8080")
    
    println("********************" + simpleClient.hello(User("Mushtaq")))
    simpleClient.booksListing().foreach(x => println("****" + x))
    
    never
    