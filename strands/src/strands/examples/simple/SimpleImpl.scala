package strands.examples.simple

import SimpleModels.*
import ox.flow.Flow

import scala.concurrent.duration.{DurationInt, FiniteDuration}

def SimpleImpl(): SimpleApi = {
  def tick(interval: FiniteDuration) = Flow.tick(interval).map(_ => Timestamp.now())

  (
    hello = (user: User) => s"Hello ${user.name}",
    booksListing = () => SimpleModels.books,
    ticks = (interval: FiniteDuration) => tick(interval),
    ticks2 = () => tick(100.millis)
  )
}
