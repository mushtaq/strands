package strands.rpc

import strands.rpc.RichEndpoint.{F0, F1}
import sttp.tapir.json.pickler.Pickler

import scala.compiletime.summonAll


class EndpointFactory[F](val make: (name: String) => RichEndpoint)

object EndpointFactory:
  given [O: Pickler] => EndpointFactory[() => O] = EndpointFactory(F0[O])
  given [I: Pickler, O: Pickler] => EndpointFactory[I => O] = EndpointFactory(F1[I, O])

  inline def from[API <: NamedTuple.AnyNamedTuple]: Map[String, RichEndpoint] =
    type Fs = NamedTuple.DropNames[API]
    type EPs = Tuple.Map[Fs, [f] =>> EndpointFactory[f]]

    val names = namesOf[API]
    val endpoints = summonAll[EPs].toList.asInstanceOf[List[EndpointFactory[?]]]
    names
      .zip(endpoints)
      .toMap
      .map:
        case name -> ef => name -> ef.make(name)
