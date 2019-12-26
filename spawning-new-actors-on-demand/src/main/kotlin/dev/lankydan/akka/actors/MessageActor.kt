package dev.lankydan.akka.actors

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.Signal
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import java.util.concurrent.ConcurrentHashMap
import kotlin.contracts.contract

class MessageActor(
  private val name: String,
  private val actors: MutableMap<String, ActorRef<Message>>,
  private val context: ActorContext<Message>
) {
  interface Message {
    data class Post(val message: String, val to: ActorRef<Message>) : Message
    data class Reply(val message: String, val from: ActorRef<Message>) : Message
  }

  fun behavior(): Behavior<Message> {
    return Behaviors.receive(Message::class.java)
      .onMessage(Message.Post::class.java, ::onPost)
      .onMessage(Message.Reply::class.java, ::onReply)
      .onSignal(PostStop::class.java) { onStop() }
      .build()
  }

  private fun onPost(post: Message.Post): Behavior<Message> {
    context.log.info("Sending message ${post.message} to ${post.to}")
    post.to.tell(Message.Reply(post.message, context.self))
    return Behaviors.stopped()
  }

  private fun onReply(reply: Message.Reply): Behavior<Message> {
    context.log.info("Received message ${reply.message} from ${reply.from}")
    return Behaviors.stopped()
  }

  private fun onStop(): Behavior<Message> {
    actors.remove(name)
    context.log.info("actors: $actors")
    return Behaviors.same()
  }

  companion object {
//    fun create(): Behavior<Message> = Behaviors.setup {
//      val key = ServiceKey.create(Message::class.java, "message_actor")
////      it.system.receptionist().tell(Receptionist.register(key, it.self))
//      MessageActor(it).behavior()
//    }

    fun create(name: String, actors: MutableMap<String, ActorRef<Message>>): Behavior<Message> = Behaviors.setup {
      val key = ServiceKey.create(Message::class.java, "message_actor")
//      it.system.receptionist().tell(Receptionist.register(key, it.self))
      MessageActor(name, actors, it).behavior()
    }
  }
}