package strands.examples.simple

import sttp.tapir.json.pickler.*

object SimpleModels:
  case class User(name: String) derives Pickler
  case class Author(name: String) derives Pickler
  case class Book(title: String, year: Int, author: Author) derives Pickler

  val books = List(
    Book("The Sorrows of Young Werther", 1774, Author("Johann Wolfgang von Goethe")),
    Book("On the Niemen", 1888, Author("Eliza Orzeszkowa")),
    Book("The Art of Computer Programming", 1968, Author("Donald Knuth")),
    Book("Pharaoh", 1897, Author("Boleslaw Prus"))
  )
