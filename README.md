# Strands RPC (PoC)

A tiny Scala 3 proof‑of‑concept showing how to define an RPC API as a named tuple, serve it over HTTP, and call it from a type‑safe client — in direct style — with optional actor serialization for thread safety, plus simple SSE streaming.

Status: PoC, for educational purposes only.

## What it provides

- Define your API as a Scala 3 named tuple of functions.
- Generate Tapir server endpoints and a type‑safe client from that API.
- Choose between direct execution or actor‑serialized execution (thread‑safe) without changing your service code.
- Support for unary calls and server‑sent events (SSE) streaming using Ox `Flow`.

Built on: Tapir, sttp client, upickle, Ox.

## Design goals (PoC)

- Direct‑style programming: synchronous functions and straightforward concurrency with Ox (`par`, `supervised`).
- Minimal boilerplate: API as a named tuple; endpoints and client derived.
- Opt‑in thread safety: flip to actor‑serialized execution without changing impl code.
- Narrow scope: unary and SSE only; keep the surface small.

## Repo layout

- `rpc/` — core library (endpoint derivation, client, helpers).
- `examples/` — small runnable examples (simple + actor variants).
- `examples/test/` — tests that double as usage demos.

## Requirements

- Use the provided `./mill` script (Mill 1.x). It manages a suitable JDK per build settings; no extra setup should be needed.

## How to run

Run the simple example server (serves `SimpleApi`):

```bash
./mill examples.runMain strands.examples.simple.SimpleRpcMain
```

Run the actor example server and client in two terminals:

```bash
# Terminal 1: start server
./mill examples.runMain strands.examples.actor.ActorRpcServer

# Terminal 2: run client
./mill examples.runMain strands.examples.actor.ActorRpcClient
```

Run tests (includes actor vs non‑actor concurrency demo and streaming):

```bash
./mill examples.test
./mill rpc.test
```

You can open Swagger UI at `http://localhost:<port>/docs` when a server is running.
Prometheus metrics are exposed at `http://localhost:<port>/metrics`.

## Define an API (named tuples)

Minimal example (see `examples/src/…/actor/BankAccountApi.scala`):

```scala
package strands.examples.actor

type BankAccountApi = (
  getBalance: () => Int,
  deposit: (amount: Int) => Unit
)

def BankAccount(): BankAccountApi =
  var balance = 0
  (
    getBalance = () => balance,
    deposit    = (amount: Int) => balance += amount
  )
```

## Serving the API

- Direct (no serialization):

```scala
import strands.rpc.{RpcServer, Service}

val endpoints = Service.simpleEndpoints(BankAccount())
RpcServer.start(endpoints, port = 8080, name = "bank", version = "0.1.0")
```

- Actor‑serialized (thread‑safe without changing `BankAccount()`):

```scala
import ox.*
import strands.rpc.{RpcServer, Service}

supervised:
  val endpoints = Service.actorEndpoints(BankAccount())
  RpcServer.start(endpoints, port = 8081, name = "bank-actor", version = "0.1.0")
```

## Calling the API (client)

Create a client from a base URI and call methods in direct style:

```scala
import strands.rpc.Client
import sttp.model.Uri.UriContext

val bank: Client[BankAccountApi] = Client.of(uri"http://localhost:8081")
bank.deposit(100)
val b = bank.getBalance()
```

You can also create a client against a Tapir stub backend in tests. See `examples/test/src/…/TestHelpers.scala`.

## Concurrency demo (race vs actor)

`examples/test/src/strands/examples/actor/ConcurrentRpcTest.scala` shows:

- With `Service.simpleEndpoints(...)`, 1000 parallel deposits race; final balance is not 1000.
- With `Service.actorEndpoints(...)`, the same code becomes serialized and the final balance is 1000.

Run: `./mill examples.test`

## Streaming (SSE)

Return `ox.flow.Flow[T]` to expose an SSE stream; the client receives an `Ox` `Flow[T]`:

```scala
// API
import ox.flow.Flow

type SimpleApi = (
  ticks: (interval: FiniteDuration) => Flow[Timestamp]
)

// Server: Service.simpleEndpoints(SimpleImpl())
// Client: val c: Client[SimpleApi] = Client.of(uri)
val xs = c.ticks(100.millis).take(3)
xs.runForeach(println)
```

Full example in `examples/src/strands/examples/simple/SimpleApi.scala` and tests in `examples/test/src/strands/examples/simple/SimpleRpcTest.scala`.

## Named tuples and multiple parameters (join)

Multiple parameters are represented as a single named tuple argument and serialized via an upickle addon included in `build.mill` (`upickle-implicits-named-tuples`).

API and implementation (excerpt from `SimpleApi`):

```scala
type SimpleApi = (
  join: ((first: String, second: String)) => String
)

def SimpleImpl(): SimpleApi = (
  // ...
  join = parts => parts.first + " " + parts.second
)
```

Client usage (named args, requires the named-tuple implicits):

```scala
import upickle.implicits.namedTuples.default.given
simpleClient.join(first = "Mushtaq", second = "Ahmed") // => "Mushtaq Ahmed"
```

## Troubleshooting

- Named tuples JSON codecs
  - Ensure the addon is on the classpath (already in `build.mill`: `upickle-implicits-named-tuples`).
  - Import the implicits where you (de)serialize named tuples (server launcher, client code, tests):
    `import upickle.implicits.namedTuples.default.given`.
  - Missing this import typically results in compile‑time missing instances or runtime decode errors.

- Tapir Schema derivation
  - For case classes/ADTs used in your API, import: `import sttp.tapir.generic.auto.*`.
  - Add it in files where endpoints are built (e.g., around `Service.simpleEndpoints(...)` / `Service.actorEndpoints(...)`) or in tests using those models.
  - Alternatively, provide explicit `given Schema[YourType]` if you don’t want automatic derivation.

## Inspiration

This experiment is inspired by Named Tuples in Scala 3: https://bishabosha.github.io/articles/named-tuples.html and the corresponding Scalar 2025 talk.

## Notes and limitations

- Supported function shapes: `() => O`, `I => O`, `() => Flow[O]`, `I => Flow[O]`.
- Endpoint names come from named‑tuple field names; renaming changes routes.
- Client dispatch uses field names (runtime lookup); keep names stable for compatibility.
- Requires upickle implicits for your types; named tuples use `upickle-implicits-named-tuples`.
- This is a PoC; APIs and behavior may change. Not production‑ready.

## Use cases (suggested)

- Stateful services where single‑writer semantics via actor serialization help avoid locks.
- Simple microservices with unary RPC and lightweight streaming via SSE.
- Educational exploration of Scala 3 named tuples as a lightweight IDL.

## References (in repo)

- Actor example: `examples/src/strands/examples/actor/*`
- Simple example + streaming: `examples/src/strands/examples/simple/*`
- Core library: `rpc/src/strands/rpc/*`
