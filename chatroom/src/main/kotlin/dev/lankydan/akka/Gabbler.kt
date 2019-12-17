package dev.lankydan.akka

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors

// Does not extend [AbstractBehavior] as this example is done in a functional style
class Gabbler private constructor(private val context: ActorContext<SessionEvent>) {

  private fun behavior(): Behavior<SessionEvent> {
    return Behaviors.receive(SessionEvent::class.java)
      .onMessage(SessionDenied::class.java, ::onSessionDenied)
      .onMessage(SessionGranted::class.java, ::onSessionGranted)
      .onMessage(MessagePosted::class.java, ::onMessagePosted)
      .build()
  }

  private fun onSessionDenied(message: SessionDenied): Behavior<SessionEvent> {
    context.log.info("Cannot start chat room session: ${message.reason}")
    return Behaviors.stopped()
  }

  private fun onSessionGranted(message: SessionGranted): Behavior<SessionEvent> {
    message.handle.tell(PostMessage("Hello world!"))
    return Behaviors.same()
  }

  private fun onMessagePosted(message: MessagePosted): Behavior<SessionEvent> {
    context.log.info("Message has been posted by '${message.screenName}': ${message.message}")
    return Behaviors.stopped()
  }

  companion object {
    fun create(): Behavior<SessionEvent> = Behaviors.setup { Gabbler(it).behavior() }
  }
}