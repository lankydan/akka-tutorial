package dev.lankydan.akka

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

class HelloWorldBot private constructor(context: ActorContext<HelloWorld.Greeted>, private val max: Int) :
  AbstractBehavior<HelloWorld.Greeted>(context) {

  private var counter = 0

  override fun createReceive(): Receive<HelloWorld.Greeted> {
    // Call [onGreeted] when receiving [Greeted] messages
    return newReceiveBuilder().onMessage(HelloWorld.Greeted::class.java, ::onGreeted).build()
  }

  private fun onGreeted(message: HelloWorld.Greeted): Behavior<HelloWorld.Greeted> {
    counter++
    context.log.info("Greeting $counter for ${message.whom}")
    if(counter == max) {
      // Stops the actor
      return Behaviors.stopped()
    } else {
      // Send a new message to the sender of the original message ([message.from])
      // Set [replyTo] as [context.self] so the other actor has a reference to this actor
      message.from.tell(HelloWorld.Greet(message.whom, context.self))
    }
    return this
  }

  companion object {
    // [Behaviors.setup] is a factory method to define the behavior of an actor
    fun create(max: Int): Behavior<HelloWorld.Greeted> = Behaviors.setup { HelloWorldBot(it, max) }
  }
}