package strands.experimental

import java.util.concurrent.StructuredTaskScope
import java.util.concurrent.StructuredTaskScope.Joiner
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.Using

extension [A](list: Iterable[A])
  def mapPar[B](f: A => B): List[B] =
    Using.resource(
      StructuredTaskScope.open(
        Joiner.allSuccessfulOrThrow[B]
      )
    ): scope =>
      list.foreach(a => scope.fork(() => f(a)))
      scope.join().map(_.get()).toList.asScala.toList
