package strands.rpc.common

import sttp.client4.StreamBackend
import sttp.shared.Identity
import sttp.tapir.Schema
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.sync.OxStreams

import scala.compiletime.constValueTuple
import scala.concurrent.duration.{Duration, FiniteDuration}

type RpcEndpoints = List[ServerEndpoint[OxStreams, Identity]]
type RpcBackend = StreamBackend[Identity, OxStreams]

object RpcHelpers extends TapirJsonUpickle with SseHelpers:
  inline def namesOf[NT <: NamedTuple.AnyNamedTuple]: List[String] =
    type Ns = NamedTuple.Names[NT]
    constValueTuple[Ns].toList match
      case names: List[String] => names
  
  given Schema[FiniteDuration] =
    summon[Schema[Duration]].as[FiniteDuration]
