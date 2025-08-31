package strands.rpc

import sttp.tapir.*
import sttp.tapir.json.pickler.{Pickler, jsonBody}

import scala.compiletime.summonAll

sealed trait EndpointFactory[F]:
  type In: Pickler
  type Out: Pickler
  type Fn = F

  def create(name: String): PublicEndpoint[In, Unit, Out, Any] =
    endpoint.post
      .in(name)
      .in(jsonBody[In])
      .out(jsonBody[Out])

  def adaptToApi(f: In => Out): Fn
  def adaptFromApi(f: Fn): In => Out

object EndpointFactory:
  given [O: Pickler] => EndpointFactory[() => O] = new:
    type In = Unit
    type Out = O
    def adaptToApi(f: In => Out) = () => f(())
    def adaptFromApi(f: () => O) = _ => f()

  given [I: Pickler, O: Pickler] => EndpointFactory[I => O] = new:
    type In = I
    type Out = O
    def adaptToApi(f: In => Out) = f
    def adaptFromApi(f: In => Out) = f

  inline def of[API <: NamedTuple.AnyNamedTuple]: Map[String, EndpointFactory[?]] =
    type Fs = NamedTuple.DropNames[API]
    type EPs = Tuple.Map[Fs, [f] =>> EndpointFactory[f]]

    val names = namesOf[API]
    val endpoints = summonAll[EPs].toList.asInstanceOf[List[EndpointFactory[?]]]
    names.zip(endpoints).toMap
