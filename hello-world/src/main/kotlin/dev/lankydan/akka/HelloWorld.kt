package dev.lankydan.akka

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

class HelloWorld private constructor(context: ActorContext<Greet>) : AbstractBehavior<HelloWorld.Greet>(context) {

  data class Greet(val whom: String, val replyTo: ActorRef<Greeted>)
  data class Greeted(val whom: String, val from: ActorRef<Greet>)

  override fun createReceive(): Receive<Greet> {
    // Call [onGreet] when receiving [Greet] messages
    return newReceiveBuilder().onMessage(Greet::class.java, ::onGreet).build()
  }

  private fun onGreet(command: Greet): Behavior<Greet> {
    context.log.info("Hello ${command.whom}!")
    // Send a new message to the actor defined by [command.replyTo]
    // Set [from] as [context.self] so the other actor has a reference to this actor
    command.replyTo.tell(Greeted(command.whom, context.self))
    return this
  }

  companion object {
    // [Behaviors.setup] is a factory method to define the behavior of an actor
    fun create(): Behavior<Greet> = Behaviors.setup(::HelloWorld)
  }
}