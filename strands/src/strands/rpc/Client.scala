package strands.rpc

import sttp.client4.{Backend, DefaultSyncBackend}
import sttp.model.Uri
import sttp.shared.Identity
import sttp.tapir.PublicEndpoint
import sttp.tapir.client.sttp4.SttpClientInterpreter

class Client[API <: NamedTuple.AnyNamedTuple](
    endpointFactories: Map[String, EndpointFactory[?]],
    interpreter: SttpClientInterpreter,
    baseUri: Option[Uri],
    backend: Backend[Identity]
) extends Selectable:

  type Fields = API

  inline def selectDynamic(name: String): Any =
    val endpointFactory = endpointFactories(name)
    val endpoint = endpointFactory.create(name)
    val clientFn = clientFunction(endpoint)
    endpointFactory.adaptToApi(clientFn)

  private def clientFunction[I, E, O](e: PublicEndpoint[I, E, O, Any]): I => O =
    interpreter
      .toRequestThrowErrors(e, baseUri)
      .andThen: req =>
//        println(req.toCurl)
        req.send(backend).body

object Client:
  inline def from[API <: NamedTuple.AnyNamedTuple](
      interpreter: SttpClientInterpreter = SttpClientInterpreter(),
      baseUri: Option[Uri] = None,
      backend: Backend[Identity] = DefaultSyncBackend()
  ) =
    new Client[API](EndpointFactory.of[API], interpreter, baseUri, backend)
