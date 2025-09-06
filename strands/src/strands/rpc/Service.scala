package strands.rpc

import ox.Ox
import ox.channels.{Actor, ActorRef}
import sttp.shared.Identity
import sttp.tapir.server.ServerEndpoint

import scala.NamedTuple.NamedTuple as NT

object Service:
  inline def simpleEndpoints[N <: Tuple, V <: Tuple, API <: NT[N, V]](
      api: API
  ): List[ServerEndpoint[Any, Identity]] =

    serverEndpoints(api, ServiceType.Simple)

  inline def actorEndpoints[N <: Tuple, V <: Tuple, API <: NT[N, V]](
      api: API
  )(using Ox): List[ServerEndpoint[Any, Identity]] =

    val actorRef: ActorRef[Any] = Actor.create(api)
    serverEndpoints(api, ServiceType.Actor(actorRef))

  private inline def serverEndpoints[N <: Tuple, V <: Tuple, API <: NT[N, V]](
      api: API,
      serviceType: ServiceType
  ) =
    val richEndpoints = EndpointFactory.from[API]
    val names = namesOf[API]
    val functions: List[Any] = api.toList
    names
      .zip(functions)
      .map:
        case (name, fn) =>
          val endpoint = richEndpoints(name)
          endpoint.service(fn.asInstanceOf, serviceType)

