package strands.rpc

import sttp.client4.DefaultSyncBackend
import sttp.model.Uri

class Client[API <: NamedTuple.AnyNamedTuple](
    richEndpoints: Map[String, RichEndpoint],
    baseUri: Option[Uri],
    backend: RpcBackend
) extends Selectable:

  type Fields = API

  given RequestInterpreter = RequestInterpreter(baseUri, backend)

  inline def selectDynamic(name: String): Any =
    val endpoint = richEndpoints(name)
    endpoint.client

object Client:
  inline def from[API <: NamedTuple.AnyNamedTuple](
      baseUri: Option[Uri] = None,
      backend: RpcBackend = StreamingSyncBackend(DefaultSyncBackend())
  ) =
    new Client[API](EndpointFactory.from[API], baseUri, backend)
