package dev.lankydan.akka

import akka.actor.typed.ActorSystem
import akka.actor.typed.SpawnProtocol
import dev.lankydan.akka.actors.Main
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class Application {

  // Create the system/root actor
  @Bean
  fun actorSystem(): ActorSystem<SpawnProtocol.Command> = ActorSystem.create(Main.create(), "tutorial")
}

fun main() {
  SpringApplication.run(Application::class.java)
}