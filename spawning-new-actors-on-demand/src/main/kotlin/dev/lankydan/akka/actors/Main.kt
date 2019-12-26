package dev.lankydan.akka.actors

import akka.actor.typed.Behavior
import akka.actor.typed.SpawnProtocol
import akka.actor.typed.javadsl.Behaviors

class Main {
  companion object {
    fun create(): Behavior<SpawnProtocol.Command> = Behaviors.setup { SpawnProtocol.create() }
  }
}