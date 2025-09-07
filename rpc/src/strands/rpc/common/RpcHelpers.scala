package strands.rpc.common

import ox.Chunk
import ox.flow.Flow
import sttp.client4.StreamBackend
import sttp.model.sse.ServerSentEvent
import sttp.shared.Identity
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.sync.OxStreams
import sttp.tapir.{Mapping, Schema}
import upickle.*

import scala.compiletime.constValueTuple
import scala.concurrent.duration.{Duration, FiniteDuration}

type RpcEndpoints = List[ServerEndpoint[OxStreams, Identity]]
type RpcBackend = StreamBackend[Identity, OxStreams]

object RpcHelpers extends TapirJsonUpickle:
  inline def namesOf[NT <: NamedTuple.AnyNamedTuple]: List[String] =
    type Ns = NamedTuple.Names[NT]
    constValueTuple[Ns].toList match
      case names: List[String] => names

  def sseMapping[T: ReadWriter]: Mapping[Flow[ServerSentEvent], Flow[T]] =
    Mapping.from(fromSse)(toSse)

  def fromSse[T: Reader](xs: Flow[ServerSentEvent]): Flow[T] =
    xs.flatMap(x => Flow.fromIterable(x.data.map(read[T](_))))

  def toSse[T: Writer](xs: Flow[T]): Flow[ServerSentEvent] =
    xs.map(x => ServerSentEvent(Some(write[T](x))))

  extension (chunks: Flow[Chunk[Byte]])
    def asSse: Flow[ServerSentEvent] =
      chunks.linesUtf8
        .mapStatefulConcat(List.empty[String])(
          f = (lines, str) => if str.isEmpty then (Nil, List(lines)) else (lines :+ str, Nil),
          onComplete = lines => if lines.nonEmpty then Some(lines) else None
        )
        .filter(_.nonEmpty)
        .map(ServerSentEvent.parse)

    def asSseOf[T: Reader]: Flow[T] = fromSse(chunks.asSse)

  given Schema[FiniteDuration] =
    summon[Schema[Duration]].as[FiniteDuration]
