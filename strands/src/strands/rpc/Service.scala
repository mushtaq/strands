package strands.rpc

import ox.Ox
import ox.channels.{Actor, ActorRef}
import sttp.shared.Identity
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.sync.OxStreams

import scala.NamedTuple.NamedTuple as NT

object Service:
  inline def simpleEndpoints[N <: Tuple, V <: Tuple, API <: NT[N, V]](api: API): RpcEndpoints =
    endpoints(api, ServiceType.Simple)

  inline def actorEndpoints[N <: Tuple, V <: Tuple, API <: NT[N, V]](api: API)(using Ox): RpcEndpoints =
    val actorRef: ActorRef[Any] = Actor.create(api)
    endpoints(api, ServiceType.Actor(actorRef))

  private inline def endpoints[N <: Tuple, V <: Tuple, API <: NT[N, V]](api: API, st: ServiceType): RpcEndpoints =
    val richEndpoints = EndpointFactory.from[API]
    val names = namesOf[API]
    val functions: List[Any] = api.toList
    names
      .zip(functions)
      .map:
        case (name, function) =>
          val endpoint = richEndpoints(name)
          val fn = function.asInstanceOf[endpoint.F]
          endpoint.service(fn, st).asInstanceOf[ServerEndpoint[OxStreams, Identity]]
