package strands.examples.simple

import SimpleModels.*

def SimpleImpl(): SimpleApi = (
  hello = (user: User) => s"Hello ${user.name}",
  booksListing = () => SimpleModels.books
)