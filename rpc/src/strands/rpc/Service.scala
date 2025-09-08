package strands.rpc

import ox.Ox
import ox.channels.{Actor, ActorRef}
import strands.rpc.common.{RpcEndpoints, RpcHelpers}
import strands.rpc.internal.{EndpointFactory, ServiceType}
import sttp.shared.Identity
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.sync.OxStreams

import scala.NamedTuple.NamedTuple as NT

object Service:
  extension [N <: Tuple, V <: Tuple, API <: NT[N, V]](api: API)
    inline def simpleEndpoints: RpcEndpoints =
      endpoints(ServiceType.Simple)

    inline def actorEndpoints(using Ox): RpcEndpoints =
      val actorRef: ActorRef[Any] = Actor.create(api)
      endpoints(ServiceType.Actor(actorRef))

    private inline def endpoints(st: ServiceType): RpcEndpoints =
      val richEndpoints = EndpointFactory.from[API]
      val names = RpcHelpers.namesOf[API]
      val functions: List[Any] = api.toList
      names
        .zip(functions)
        .map:
          case (name, function) =>
            val endpoint = richEndpoints(name)
            val fn = function.asInstanceOf[endpoint.F]
            endpoint.service(fn, st).asInstanceOf[ServerEndpoint[OxStreams, Identity]]
