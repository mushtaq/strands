package strands.rpc

import sttp.client4.{Backend, DefaultSyncBackend}
import sttp.model.Uri
import sttp.shared.Identity

class Client[API <: NamedTuple.AnyNamedTuple](
    richEndpoints: Map[String, RichEndpoint],
    baseUri: Option[Uri],
    backend: Backend[Identity]
) extends Selectable:

  type Fields = API

  given RequestInterpreter = RequestInterpreter(baseUri, backend)

  inline def selectDynamic(name: String): Any =
    val endpoint = richEndpoints(name)
    endpoint.client

object Client:
  inline def from[API <: NamedTuple.AnyNamedTuple](
      baseUri: Option[Uri] = None,
      backend: Backend[Identity] = DefaultSyncBackend()
  ) =
    new Client[API](EndpointFactory.from[API], baseUri, backend)
