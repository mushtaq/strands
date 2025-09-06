package strands.examples.simple

import SimpleModels.*
import ox.flow.Flow

import scala.concurrent.duration.{DurationInt, FiniteDuration}

def SimpleImpl(): SimpleApi = (
  hello = (user: User) => s"Hello ${user.name}",
  booksListing = () => SimpleModels.books,
  ticks = (interval: FiniteDuration) => Flow.tick(interval).map(_ => Timestamp.now()),
  ticks2 = () => Flow.tick(100.millis).map(_ => Timestamp.now())
)
