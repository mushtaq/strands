package strands.rpc

import strands.rpc.common.RpcBackend
import strands.rpc.internal.{EndpointFactory, RequestInterpreter, RichEndpoint, StreamingSyncBackend}
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
  inline def of[API <: NamedTuple.AnyNamedTuple](baseUri: Uri): Client[API] =
    Client[API](EndpointFactory.from[API], Some(baseUri), StreamingSyncBackend(DefaultSyncBackend()))

  inline def of[API <: NamedTuple.AnyNamedTuple](backend: RpcBackend): Client[API] =
    Client[API](EndpointFactory.from[API], None, backend)
