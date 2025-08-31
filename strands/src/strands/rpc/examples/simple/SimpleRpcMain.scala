package strands.rpc.examples.simple

import ox.*
import SimpleModels.User
import strands.rpc.{Client, Rpc, Service}
import sttp.model.Uri.UriContext
import sttp.shared.Identity
import sttp.tapir.server.ServerEndpoint

object SimpleRpcMain extends OxApp.Simple:

  def run(using Ox): Unit =
    val serverEndpoints: List[ServerEndpoint[Any, Identity]] =  
      Service.simpleEndpoints(SimpleImpl())

    Rpc.startServer(serverEndpoints, 8080, "simple-example", "0.1.0")

    println()
    
    val simpleClient: Client[SimpleApi] =
      Client.from[SimpleApi](baseUri = Some(uri"http://localhost:8080"))
    
    println("********************" + simpleClient.hello(User("Mushtaq")))
    simpleClient.booksListing().foreach(x => println("****" + x))
    
    never
    