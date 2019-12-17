package dev.lankydan.akka

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

class HelloWorldMain private constructor(context: ActorContext<Start>, private val greeter: ActorRef<HelloWorld.Greet>) :
  AbstractBehavior<HelloWorldMain.Start>(context) {

  data class Start(val name: String)

  override fun createReceive(): Receive<Start> {
    // Call [onStart] when receiving [Start] messages
    return newReceiveBuilder().onMessage(Start::class.java, ::onStart).build()
  }

  private fun onStart(command: Start): Behavior<Start> {
    // [ActorContext.spawn] creates a new [HelloWorldBot] actor
    val replyTo: ActorRef<HelloWorld.Greeted> = context.spawn(HelloWorldBot.create(3), command.name)
    // The new actor has a new message placed on its queue
    greeter.tell(HelloWorld.Greet(command.name, replyTo))
    return this
  }

  companion object {
    // [Behaviors.setup] is a factory method to define the behavior of an actor
    // [ActorContext.spawn] creates a new [HelloWorld] actor with the name "greeter"
    // The [HelloWorld] actor is created through [HelloWorld.create]
    fun create(): Behavior<Start> = Behaviors.setup { context -> HelloWorldMain(context, context.spawn(HelloWorld.create(), "greeter")) }
  }
}

fun main() {
  // Creates the [HelloWorldMain] actor
  // This actor is started once the [Start] message is sent
  val system = ActorSystem.create(HelloWorldMain.create(), "hello")
  system.tell(HelloWorldMain.Start("Dan"))
  system.tell(HelloWorldMain.Start("Laura"))
}