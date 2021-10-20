package shaw.papadino

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Tourist {
  sealed trait Message
  case class Guidance(code: String, description: String) extends Message
  case class Start(codes: Seq[String]) extends Message

  def apply(
    guidebook: ActorRef[Guidebook.Message]
  ): Behavior[Tourist.Message] = {
    receive(guidebook)
  }
  def receive(
    guideBook: ActorRef[Guidebook.Message]
  ): Behavior[Tourist.Message] = Behaviors.receive { (context, message) =>
    message match {
      case Start(codes) =>
        codes.foreach(guideBook ! Guidebook.Inquiry(_, context.self))
        Behaviors.same
      case Guidance(code, description) =>
        println(s"$code, $description")
        Behaviors.same
    }
  }
}
