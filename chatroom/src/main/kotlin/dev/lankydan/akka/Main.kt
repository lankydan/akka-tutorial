package dev.lankydan.akka

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.Terminated
import akka.actor.typed.javadsl.Behaviors

// This example is created using a _functional_ style
fun create(): Behavior<Unit> {
  return Behaviors.setup { context ->
    // Create the [ChatRoom] and [Gabbler] actors
    val chatRoom = context.spawn(ChatRoom.create(), "ChatRoom")
    val gabbler = context.spawn(Gabbler.create(), "Gabbler")
    // [ActorContext.watch] watches the [Gabbler] actor to emit a [Terminated] signal once completing
    context.watch(gabbler)
    // Send a message to the [ChatRoom] actor
    chatRoom.tell(GetSession("ol, Gabbler", gabbler))

    // [BehaviorBuilder.onSignal] receives the [Terminated] signal from the [Gabbler] actor
    Behaviors.receive(Unit::class.java)
      // Forgetting to call [BehaviorBuilder.onSignal] when watching another actor will cause a
      // [DeathPactException]
      .onSignal(Terminated::class.java) { Behaviors.stopped() }
      .build()
  }
}

fun main() {
  ActorSystem.create(create(), "ChatRoomDemo")
}