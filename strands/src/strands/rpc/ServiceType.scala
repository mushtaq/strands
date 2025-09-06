package strands.rpc

import ox.channels.ActorRef

enum ServiceType:
  case Simple
  case Actor(actorRef: ActorRef[Any])

  def wrap[I, O](f: I => O): I => O = this match
    case Simple          => f
    case Actor(actorRef) => x => actorRef.ask(_ => f(x))
