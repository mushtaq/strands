package strands.rpc.internal

import strands.rpc.common.RpcBackend
import sttp.capabilities.Effect
import sttp.client4.*
import sttp.client4.impl.ox.sse.OxServerSentEvents
import sttp.monad.{IdentityMonad, MonadError}
import sttp.shared.Identity
import sttp.tapir.server.netty.sync.OxStreams

class StreamingSyncBackend(delegate: SyncBackend) extends RpcBackend:
  override val monad: MonadError[Identity] = IdentityMonad

  override def send[T](
      request: GenericRequest[T, OxStreams & Effect[Identity]]
  ): Identity[Response[T]] =
    request match
      // Non -streaming path → just delegate
      case req: Request[T] @unchecked =>
        delegate.send(req)

      // Streaming path → map InputStream to the raw Ox stream value
      case sreq: StreamRequest[T, OxStreams] @unchecked =>
        val reqNoStream: Request[T] =
          basicRequest
            .method(sreq.method, sreq.uri)
            .withHeaders(sreq.headers)
            .withOptions(sreq.options)
            .withAttributes(sreq.attributes)
            .response(asInputStreamAlways(OxServerSentEvents.parse(_).asInstanceOf[T]))

        delegate.send(reqNoStream)

  override def close(): Unit = delegate.close()
