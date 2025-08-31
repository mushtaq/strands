package strands.rpc

import ox.*
import sttp.shared.Identity
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.server.netty.sync.{NettySyncServer, NettySyncServerBinding, NettySyncServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.NamedTuple.NamedTuple as NT

object Rpc:

  def startServer[N <: Tuple, V <: Tuple, API <: NT[N, V]](
      serverEndpoints: List[ServerEndpoint[Any, Identity]],
      port: Int,
      name: String,
      version: String
  )(using Ox): NettySyncServerBinding =
    val prometheusMetrics: PrometheusMetrics[Identity] =
      PrometheusMetrics.default[Identity]()

    val metricsEndpoint: ServerEndpoint[Any, Identity] =
      prometheusMetrics.metricsEndpoint

    val docEndpoints: List[ServerEndpoint[Any, Identity]] =
      SwaggerInterpreter().fromServerEndpoints[Identity](serverEndpoints, s"$name-docs", version)

    val allEndpoints: List[ServerEndpoint[Any, Identity]] =
      serverEndpoints ++ docEndpoints ++ List(metricsEndpoint)

    val serverOptions =
      NettySyncServerOptions.customiseInterceptors
        .metricsInterceptor(prometheusMetrics.metricsInterceptor())
        .options

    val binding =
      useInScope(
        NettySyncServer(serverOptions)
          .port(port)
          .addEndpoints(allEndpoints)
          .start()
      )(_.stop())

    println(
      s"Go to http://localhost:${binding.port}/docs to open SwaggerUI. "
    )
    binding
