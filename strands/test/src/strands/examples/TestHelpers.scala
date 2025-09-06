package strands.examples

import strands.rpc.{RpcBackend, RpcEndpoints}
import sttp.client4.testing.StreamBackendStub
import sttp.monad.IdentityMonad
import sttp.tapir.server.stub4.TapirStreamStubInterpreter

object TestHelpers:
  extension (serverEndpoints: RpcEndpoints)
    def backendStub(): RpcBackend =
      TapirStreamStubInterpreter(StreamBackendStub(IdentityMonad))
        .whenServerEndpointsRunLogic(serverEndpoints)
        .backend()
