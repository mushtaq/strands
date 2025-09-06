package strands.examples.simple

import SimpleModels.*
import ox.flow.Flow

import scala.concurrent.duration.FiniteDuration

type SimpleApi = (
    hello: (user: User) => String,
    booksListing: () => List[Book],
    ticks: (interval: FiniteDuration) => Flow[Timestamp]
)
