package strands.rpc.examples.simple

import SimpleModels.*

type SimpleApi = (
    hello: (user: User) => String,
    booksListing: () => List[Book]
)
