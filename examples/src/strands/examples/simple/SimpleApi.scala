package strands.examples.simple

import ox.flow.Flow
import strands.examples.simple.SimpleModels.*

import scala.concurrent.duration.{DurationInt, FiniteDuration}

type SimpleApi = (
    hello: (user: User) => String,
    booksListing: () => List[Book],
    ticks: (interval: FiniteDuration) => Flow[Timestamp],
    ticks2: () => Flow[Timestamp],
//    join: (parts: (first: String, second: String)) => String
)


def SimpleImpl(): SimpleApi = {
  def tick(interval: FiniteDuration) = Flow.tick(interval).map(_ => Timestamp.now())

  (
    hello = (user: User) => s"Hello ${user.name}",
    booksListing = () => SimpleModels.books,
    ticks = (interval: FiniteDuration) => tick(interval),
    ticks2 = () => tick(100.millis),
//    join = (parts: (first: String, second: String)) => parts.first + " " + parts.second
  )
}
