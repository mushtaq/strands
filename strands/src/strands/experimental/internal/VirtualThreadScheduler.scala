package strands.experimental.internal

import java.lang.Thread.Builder
import java.util.concurrent.{Executor, ThreadFactory}

private[strands] object VirtualThreadScheduler:
  private val cls = Class.forName("java.lang.ThreadBuilders$VirtualThreadBuilder")
  private val ctor = cls.getDeclaredConstructor(classOf[Executor])
  ctor.setAccessible(true) // requires --add-opens java.base/java.lang=ALL-UNNAMED

  def adapt(carrier: Executor): ThreadFactory =
    val builder = ctor.newInstance(carrier).asInstanceOf[Builder.OfVirtual]
    builder.name("vt-", 0L).factory()
