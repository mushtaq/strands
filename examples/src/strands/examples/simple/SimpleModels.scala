package strands.examples.simple

import upickle.ReadWriter

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object SimpleModels:
  case class User(name: String) derives ReadWriter
  case class Author(name: String) derives ReadWriter
  case class Book(title: String, year: Int, author: Author) derives ReadWriter
  case class Timestamp(value: String) derives ReadWriter

  object Timestamp:
    def now(): Timestamp =
      val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
      Timestamp(LocalDateTime.now().format(dateTimeFormat))

  val books = List(
    Book("The Sorrows of Young Werther", 1774, Author("Johann Wolfgang von Goethe")),
    Book("On the Niemen", 1888, Author("Eliza Orzeszkowa")),
    Book("The Art of Computer Programming", 1968, Author("Donald Knuth")),
    Book("Pharaoh", 1897, Author("Boleslaw Prus"))
  )
