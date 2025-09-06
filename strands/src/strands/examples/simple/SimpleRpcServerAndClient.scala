package strands.examples.simple

import ox.*
import strands.examples.simple.SimpleModels.User
import strands.rpc.{Client, Rpc, RpcEndpoints, Service, given}
import sttp.model.Uri.UriContext

object SimpleRpcServerAndClient extends OxApp.Simple:

  def run(using Ox): Unit =
    val serverEndpoints: RpcEndpoints =
      Service.simpleEndpoints(SimpleImpl())

    Rpc.startServer(serverEndpoints, 8080, "simple-example", "0.1.0")

    println()
    
    val simpleClient: Client[SimpleApi] =
      Client.from[SimpleApi](baseUri = Some(uri"http://localhost:8080"))
    
    println("********************" + simpleClient.hello(User("Mushtaq")))
    simpleClient.booksListing().foreach(x => println("****" + x))
    
    never
    