package strands.rpc.internal

import ox.flow.Flow
import strands.rpc.common.RpcHelpers
import strands.rpc.internal.RichEndpoint.{F0, F1, FS0, FS1}
import sttp.tapir.Schema
import upickle.ReadWriter

import scala.compiletime.summonAll

class EndpointFactory[F](val make: (name: String) => RichEndpoint)

object EndpointFactory:
  given [O: {ReadWriter, Schema}] => EndpointFactory[() => O] = EndpointFactory(F0[O])
  given [I: {ReadWriter, Schema}, O: {ReadWriter, Schema}] => EndpointFactory[I => O] = EndpointFactory(F1[I, O])

  given fs0: [O: ReadWriter] => EndpointFactory[() => Flow[O]] = EndpointFactory(FS0[O])
  given fs1: [I: {ReadWriter, Schema}, O: ReadWriter] => EndpointFactory[I => Flow[O]] = EndpointFactory(FS1[I, O])

  inline def from[API <: NamedTuple.AnyNamedTuple]: Map[String, RichEndpoint] =
    type Fs = NamedTuple.DropNames[API]
    type EFs = Tuple.Map[Fs, [f] =>> EndpointFactory[f]]

    val names = RpcHelpers.namesOf[API]
    val endpoints = summonAll[EFs].toList.asInstanceOf[List[EndpointFactory[?]]]
    names
      .zip(endpoints)
      .toMap
      .map:
        case name -> ef => name -> ef.make(name)
