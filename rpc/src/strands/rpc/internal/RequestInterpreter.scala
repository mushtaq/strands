package strands.rpc.internal

import strands.rpc.common.RpcBackend
import sttp.model.Uri
import sttp.tapir.PublicEndpoint
import sttp.tapir.client.sttp4.SttpClientInterpreter
import sttp.tapir.client.sttp4.stream.StreamSttpClientInterpreter
import sttp.tapir.server.netty.sync.OxStreams

class RequestInterpreter(
    baseUri: Option[Uri],
    backend: RpcBackend
):
  protected val simpleInterpreter: SttpClientInterpreter = SttpClientInterpreter()
  protected val streamInterpreter: StreamSttpClientInterpreter = StreamSttpClientInterpreter()

  extension [I, E, O](e: PublicEndpoint[I, E, O, Any])
    def ask(input: I): O =
      val reqF = simpleInterpreter.toRequestThrowErrors(e, baseUri)
      backend.monad.map {
        val req = reqF(input)
        //        println(req.toCurl)
        req.send(backend)
      }(_.body)

  extension [I, E, O](e: PublicEndpoint[I, E, O, OxStreams])
    def stream(input: I): O =
      val reqF = streamInterpreter.toRequestThrowErrors[I, E, O, OxStreams](e, baseUri)
      backend.monad.map {
        val req = reqF(input)
//        println(req.toCurl)
        req.send(backend)
      }(_.body)
