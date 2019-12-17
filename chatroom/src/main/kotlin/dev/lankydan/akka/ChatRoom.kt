package dev.lankydan.akka

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Does not extend [AbstractBehavior] as this example is done in a functional style
class ChatRoom private constructor(private val context: ActorContext<RoomCommand>) {

  data class PublishSessionMessage(val screenName: String, val message: String) : RoomCommand

  // Returns a [Behavior] so that [Behaviors.setup] can use this function
  private fun chatRoom(sessions: List<ActorRef<SessionCommand>>): Behavior<RoomCommand> {
    return Behaviors.receive(RoomCommand::class.java)
      .onMessage(GetSession::class.java) { getSession -> onGetSession(sessions, getSession) }
      .onMessage(PublishSessionMessage::class.java) { pub -> onPublishSessionMessage(sessions, pub) }
      .build()
  }

  @Throws(UnsupportedEncodingException::class)
  private fun onGetSession(sessions: List<ActorRef<SessionCommand>>, getSession: GetSession): Behavior<RoomCommand> {
    val client = getSession.replyTo
    val ses = context.spawn(
      Session.create(context.self, getSession.screenName, client),
      URLEncoder.encode(getSession.screenName, StandardCharsets.UTF_8.name())
    )
    // Replies to the actor that sent the [GetSession] message
    // Akka tutorial comment - narrow to only expose PostMessage
    client.tell(SessionGranted(ses.narrow()))
    return chatRoom(sessions + ses)
  }

  private fun onPublishSessionMessage(sessions: List<ActorRef<SessionCommand>>, pub: PublishSessionMessage): Behavior<RoomCommand> {
    val notification = NotifyClient(MessagePosted(pub.screenName, pub.message))
    sessions.forEach { it.tell(notification) }
    // [Behaviors.same] returns the same behavior as it does not need to change
    // In [HelloWorld] this was returning [this] as classes were extending [AbstractBehavior]
    return Behaviors.same()
  }

  class Session {
    companion object {
      fun create(room: ActorRef<RoomCommand>, screenName: String, client: ActorRef<SessionEvent>): Behavior<SessionCommand> {
        return Behaviors.receive(SessionCommand::class.java)
          .onMessage(PostMessage::class.java) { post -> onPostMessage(room, screenName, post) }
          .onMessage(NotifyClient::class.java) { notification -> onNotifyClient(client, notification) }
          .build()
      }

      private fun onPostMessage(
        room: ActorRef<RoomCommand>,
        screenName: String,
        post: PostMessage
      ): Behavior<SessionCommand> {
        // from client, publish to others via the room
        room.tell(PublishSessionMessage(screenName, post.message))
        return Behaviors.same()
      }

      private fun onNotifyClient(
        client: ActorRef<SessionEvent>,
        notification: NotifyClient
      ): Behavior<SessionCommand> {
        // published from the room
        client.tell(notification.message)
        return Behaviors.same()
      }
    }
  }

  companion object {
    // [Behaviors.setup] is a factory method to define the behavior of an actor
    fun create(): Behavior<RoomCommand> = Behaviors.setup { ChatRoom(it).chatRoom(mutableListOf()) }
  }
}