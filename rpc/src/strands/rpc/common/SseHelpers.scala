package strands.rpc.common

import ox.Chunk
import ox.flow.Flow
import sttp.model.sse.ServerSentEvent
import sttp.tapir.Mapping
import upickle.*

trait SseHelpers:
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
