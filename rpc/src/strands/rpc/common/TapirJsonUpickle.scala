package strands.rpc.common

import sttp.tapir

import scala.util.{Failure, Success, Try}
import sttp.tapir.Codec.JsonCodec
import sttp.tapir.DecodeResult.Error.JsonDecodeException
import sttp.tapir.DecodeResult.{Error, Value}
import sttp.tapir.{Codec, CodecFormat, EndpointIO, EndpointInput, Schema}
import upickle.default.{ReadWriter, read, write}

trait TapirJsonUpickle:
  def jsonBody[T: {ReadWriter, Schema}]: EndpointIO.Body[String, T] =
    tapir.stringBodyUtf8AnyFormat(readWriterCodec[T])

  def jsonBodyWithRaw[T: {ReadWriter, Schema}]: EndpointIO.Body[String, (String, T)] =
    tapir.stringBodyUtf8AnyFormat(summon[JsonCodec[(String, T)]])

  def jsonQuery[T: {ReadWriter, Schema}](name: String): EndpointInput.Query[T] =
    given JsonCodec[T] = readWriterCodec[T]
    val q: Codec[List[String], T, CodecFormat.Json] = Codec.listHead
    tapir.queryAnyFormat[T, CodecFormat.Json](name, q)

  implicit def readWriterCodec[T: {ReadWriter, Schema}]: JsonCodec[T] =
    Codec.json[T] { s =>
      Try(read[T](s)) match
        case Success(v) => Value(v)
        case Failure(e) => Error(s, JsonDecodeException(errors = List.empty, e))
    } { t => write(t) }
