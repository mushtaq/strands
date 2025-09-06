package strands.rpc

import ox.flow.Flow
import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.json.pickler.{Pickler, jsonBody}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.sync.{OxStreams, serverSentEventsBody}
import RpcHelpers.given 

trait RichEndpoint:
  type I
  type O
  type R
  type F

  type E = PublicEndpoint[I, Unit, O, R]

  def name: String
  def e: E
  def client(using RequestInterpreter): F
  def adapt(f: F): I => O

  def service(f: F, serviceType: ServiceType): ServerEndpoint[R, Identity] =
    e.handleSuccess(serviceType.wrap(adapt(f)))

object RichEndpoint:
  case class F0[Out: Pickler](name: String) extends RichEndpoint:
    type I = Unit
    type O = Out
    type R = Any
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

    override def adapt(f: F): I => O = f

  case class FS0[Out: Pickler](name: String) extends RichEndpoint:
    type I = Unit
    type O = Flow[Out]
    type R = OxStreams
    type F = () => O

    val e: E = endpoint.get
      .in(name)
      .out(serverSentEventsBody.map(RpcHelpers.sseMapping[Out]))

    def client(using RequestInterpreter): F = () => e.stream(())
    def adapt(f: F): I => O = _ => f()

  case class FS1[In: Pickler, Out: Pickler](name: String) extends RichEndpoint:
    type I = In
    type O = Flow[Out]
    type R = OxStreams
    type F = I => O

    val e: E = endpoint.get
      .in(name)
      .in(jsonBody[I])
      .out(serverSentEventsBody.map(RpcHelpers.sseMapping[Out]))

    def client(using RequestInterpreter): F = e.stream
    def adapt(f: F): I => O = f
