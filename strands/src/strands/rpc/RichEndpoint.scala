package strands.rpc

import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.json.pickler.{Pickler, jsonBody}
import sttp.tapir.server.ServerEndpoint

trait RichEndpoint:
  type I: Pickler
  type O: Pickler
  //  type R
  type F
  type E = PublicEndpoint[I, Unit, O, Any]
  def name: String
  def e: E
  def client(using RequestInterpreter): F
  def adapt(f: F): I => O

  def service(f: F, serviceType: ServiceType): ServerEndpoint[Any, Identity] =
    e.handleSuccess(serviceType.adapt(adapt(f)))

object RichEndpoint:
  case class F0[Out: Pickler](name: String) extends RichEndpoint:
    type I = Unit
    type O = Out
    //    type R = Any
    type F = () => O

    val e: E = endpoint.post
      .in(name)
      .out(jsonBody[O])

    def client(using RequestInterpreter): F = () => e.ask(())
    def adapt(f: F): I => O = _ => f()

  case class F1[In: Pickler, Out: Pickler](name: String) extends RichEndpoint:
    type I = In
    type O = Out
    type R = Any
    type F = In => O

    val e: E = endpoint.post
      .in(name)
      .in(jsonBody[I])
      .out(jsonBody[O])

    def client(using RequestInterpreter): F = e.ask
    def service(f: F): ServerEndpoint[Any, Identity] = e.handleSuccess(f)

    override def adapt(f: In => Out): In => Out = f
