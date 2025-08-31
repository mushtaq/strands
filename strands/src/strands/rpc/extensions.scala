package strands.rpc

import scala.compiletime.constValueTuple
import sttp.client4.{ResponseAs, asInputStreamAlways}
import sttp.tapir.json.pickler.Pickler
import upickle.default
import upickle.default.{Reader, Writer}

inline def namesOf[NT <: NamedTuple.AnyNamedTuple]: List[String] = 
  type Ns = NamedTuple.Names[NT]
  constValueTuple[Ns].toList.asInstanceOf[List[String]]

def asJson[T: Reader]: ResponseAs[T] = asInputStreamAlways(default.read[T](_))

given picklerToReader: [T] => (p: Pickler[T]) => Reader[T] =
p.innerUpickle.reader.asInstanceOf[Reader[T]]

given picklerToWriter: [T] => (p: Pickler[T]) => Writer[T] =
p.innerUpickle.writer.asInstanceOf[Writer[T]]

