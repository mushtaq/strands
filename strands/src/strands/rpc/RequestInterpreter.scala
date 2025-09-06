package strands.rpc

import sttp.model.Uri
import sttp.tapir.PublicEndpoint
import sttp.tapir.client.sttp4.SttpClientInterpreter
import sttp.tapir.client.sttp4.stream.StreamSttpClientInterpreter
import sttp.tapir.server.netty.sync.OxStreams

class RequestInterpreter(
    baseUri: Option[Uri] = None,
    backend: RpcBackend
):
  protected val simpleInterpreter: SttpClientInterpreter = SttpClientInterpreter()
  protected val streamInterpreter: StreamSttpClientInterpreter = StreamSttpClientInterpreter()

  extension [I, E, O](e: PublicEndpoint[I, E, O, Any])
    def ask(input: I): O =
      simpleInterpreter
        .toRequestThrowErrors(e, baseUri)
        .andThen: req =>
          //        println(req.toCurl)
          req.send(backend).body
        .apply(input)


  extension [I, E, O](e: PublicEndpoint[I, E, O, OxStreams])
    def stream(input: I): O = streamInterpreter.toClientThrowErrors(e, baseUri, backend).apply(input)
