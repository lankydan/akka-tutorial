package dev.lankydan.akka.web

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Props
import akka.actor.typed.SpawnProtocol
import akka.actor.typed.javadsl.AskPattern
import akka.japi.function.Function
import dev.lankydan.akka.actors.MessageActor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.util.*
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/messages")
class MessageController(private val system: ActorSystem<SpawnProtocol.Command>) {

  val actors = ConcurrentHashMap<String, ActorRef<MessageActor.Message>>()

  @PostMapping
  fun post(@RequestBody message: String): ResponseEntity<String> {
    // Spawn 2 new actors that interact with each other whenever a new request comes in
    val sender: CompletionStage<ActorRef<MessageActor.Message>> = spawnMessageActor("sender-actor-${UUID.randomUUID()}")
    val recipient: CompletionStage<ActorRef<MessageActor.Message>> = spawnMessageActor("recipient-actor-${UUID.randomUUID()}")
    // When the actors have been spawned, start sending messages
    sender.whenComplete { senderRef, exception ->
      if (exception == null) {
        recipient.whenComplete { recipientRef, exception ->
          if (exception == null) {
            senderRef.tell(MessageActor.Message.Post(message, recipientRef))
          }
        }
      }
    }
    return ResponseEntity.ok("ok")
  }

  // Spawns a new actor using the [SpawnProtocol]
  private fun spawnMessageActor(name: String): CompletionStage<ActorRef<MessageActor.Message>> {
    // Asks the [system] actor to spawn a new [MessageActor]
    return AskPattern.ask<SpawnProtocol.Command, ActorRef<MessageActor.Message>>(
      system,
      // This line is super ugly and looks better in Java...
      Function<ActorRef<ActorRef<MessageActor.Message>>, SpawnProtocol.Command> { replyTo: ActorRef<ActorRef<MessageActor.Message>> ->
        SpawnProtocol.Spawn(
          // Specify the behavior that the new actor should have
          MessageActor.create(name, actors),
          name,
          Props.empty(),
          // Sends the spawned actor's ref to the actor that [replyTo] references
          replyTo
        )
      },
      Duration.ofSeconds(3),
      system.scheduler()
    ).whenComplete { ref, exception ->
      if (exception == null) {
        actors[name] = ref
        system.log().info("actors $actors")
      }
    }
  }
}