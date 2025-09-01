//package strands.experimental
//
//import strands.rpc.examples.simple.SimpleModels.{Book, User}
//import strands.rpc.examples.simple.{SimpleApi, SimpleImpl, SimpleModels}
//import strands.rpc.{*, given}
//import sttp.tapir.*
//import sttp.tapir.json.pickler.*
//import utest.*
//
//import scala.util.TupledFunction
//
//object TestTest extends TestSuite:
//
//  val impl: SimpleApi = SimpleImpl()
//
//  extension [F, Args <: Tuple, R](f: F)
//    def tupled(using tf: TupledFunction[F, Args => R]): Args => R = tf.tupled(f)
//
//  extension [F, Args <: Tuple, R](f: Args => R)
//    def untupled(using tf: TupledFunction[F, Args => R]): F = tf.untupled(f)
//
//  val hello = endpoint.post
//    .in("hello")
////    .in(jsonBody[(name: Int)])
//    .out(jsonBody[String])
//
//  asJson[String]
////  asJson[(Int, String)]
//
//  lazy val AA = {
//
//    type SimpleApi = (
//      fn: (user: User, age: Int) => String,
//      hello: (user: User) => String,
//      booksListing: () => List[Book],
//    )
//
//    type SimpleApiTupledNamed =
//      (
//        fn: (message: (user: User, age: Int)) => String,
//        hello: (message: (user: User)) => String,      // 1‑field named tuple
//        booksListing: (message: Unit) => List[Book] // there is no "named" 0‑tuple
//      )
//
//    def SimpleImpl(): SimpleApiTupledNamed = (
//      fn = (message: (user: User, age: Int)) => "fn123", //compile error
//      hello = (message: (user: User)) => "hello123", //compile error
//      booksListing = (message: Unit) => SimpleModels.books //compile error
//    )
//
//    val instance = SimpleImpl()
//
//    instance.fn((User("abc"), 23))
//    instance.hello((user = User("abc")))
//    instance.booksListing(())
//
//    type NFN = (user: User, age: Int) => String
//
//    type TP = (User, Int)
//    type NTP = (user: User, age: Int)
//
//    val fn: (user: User, age: Int) => String = ???
//    val tupledFn: ((User, Int)) => String = fn.tupled
//    val tupledFn2: ((user: User, age: Int)) => String = x => fn.tupled(x)
//
//    tupledFn2(user = User("abc"), age = 23)
//
//    val f: (User, Int) => String = fn
//    val tupledF: ((User, Int)) => String = f.tupled
//    val tupledFn3: ((user: User, age: Int)) => String = x => f.tupled(x)
//  }
//  type FN = (User, Int) => String
//
//  val tests =
//    Tests:
//      test("tupled functions"):
//        println()
//
