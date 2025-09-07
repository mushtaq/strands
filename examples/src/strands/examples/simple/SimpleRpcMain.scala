package strands.examples.simple

import ox.*
import strands.examples.simple.SimpleModels.User
import strands.rpc.{Client, RpcServer, Service}
import sttp.model.Uri.UriContext
import strands.rpc.common.RpcHelpers.given
import strands.rpc.common.RpcEndpoints
import sttp.tapir.generic.auto.*
import upickle.implicits.namedTuples.default.given

object SimpleRpcMain extends OxApp.Simple:

  def run(using Ox): Unit =
    val serverEndpoints: RpcEndpoints =
      Service.simpleEndpoints(SimpleImpl())

    RpcServer.start(serverEndpoints, 8080, "simple-example", "0.1.0")

    println()
    
    val simpleClient: Client[SimpleApi] =
      Client.of[SimpleApi](uri"http://localhost:8080")
    
    println("********************" + simpleClient.hello(User("Mushtaq")))
    simpleClient.booksListing().foreach(x => println("****" + x))
    
    never
    