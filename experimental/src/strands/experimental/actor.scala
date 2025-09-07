package strands.experimental

import java.util.concurrent.StructuredTaskScope.{Joiner, Subtask}
import java.util.concurrent.{Callable, Executors, ScheduledExecutorService, StructuredTaskScope}
import scala.util.Using

class ActorRef[A] private[strands] (underlying: A, executorService: Option[ScheduledExecutorService] = None) extends AutoCloseable:
  private val single = executorService.getOrElse(Executors.newSingleThreadScheduledExecutor())
  private val threadFactory = internal.VirtualThreadScheduler.adapt(single)

  def ask[B](f: A => B): B =
    Using.resource(
      StructuredTaskScope.open[Any, Void](
        Joiner.awaitAllSuccessfulOrThrow,
        _.withThreadFactory(threadFactory)
      )
    ): scope =>
      val sub: Subtask[B] = scope.fork(() => f(underlying))
      scope.join()
      sub.get

  def close(): Unit =
    single.shutdown()


class ActorSystem extends AutoCloseable:
  protected[strands] val SharedExecutor: Option[ScheduledExecutorService] = None

  private class State:
    private var actorRefs = Set.empty[ActorRef[?]]

    def spawn[A](underlying: A): ActorRef[A] =
      val ref = new ActorRef(underlying, SharedExecutor)
      actorRefs += ref
      ref

    def close(): Unit = actorRefs.foreach(_.close())


  private val stateRef = new ActorRef(new State(), SharedExecutor)

  def spawn[A](underlying: A): ActorRef[A] =
    stateRef.ask(_.spawn(underlying))

  def close(): Unit =
    stateRef.ask(_.close())

class TestActorSystem extends ActorSystem:
  override protected[strands] val SharedExecutor: Option[ScheduledExecutorService] =
    Some(Executors.newSingleThreadScheduledExecutor())
