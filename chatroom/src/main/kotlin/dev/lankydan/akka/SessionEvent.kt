package dev.lankydan.akka

import akka.actor.typed.ActorRef

interface SessionEvent

data class SessionGranted(val handle: ActorRef<PostMessage>) : SessionEvent
data class SessionDenied(val reason: String) : SessionEvent
data class MessagePosted(val screenName: String, val message: String) : SessionEvent