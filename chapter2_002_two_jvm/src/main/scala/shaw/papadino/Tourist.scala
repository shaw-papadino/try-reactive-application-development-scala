package shaw.papadino

import java.util.Locale

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.util.Random
import scala.concurrent.duration._

object Tourist {
  sealed trait Message extends CborSerializable
  sealed case class Guidance(code: String, description: String) extends Message
  sealed case class Start(codes: Seq[String]) extends Message

  // messageAdapter
  case class GuidebookUpdated(newGuidebooks: Set[ActorRef[Guidebook.Message]])
      extends Message

  def apply(): Behavior[Tourist.Message] =
    Behaviors.withTimers { timer =>
      Behaviors.setup { context =>
        val subscriptionAdapter = context.messageAdapter[Receptionist.Listing] {
          case Guidebook.GuidebookKey.Listing(guidebooks) =>
            GuidebookUpdated(guidebooks)
        }

        // Subscribe
        context.system.receptionist ! Receptionist.Subscribe(
          Guidebook.GuidebookKey,
          subscriptionAdapter
        )
        timer.startTimerAtFixedRate(Start(Locale.getISOCountries), 5.seconds)
        receive(IndexedSeq.empty)
      }
    }
  def random(max: Int): Int = new Random().nextInt(max)

  def receive(
    guidebooks: IndexedSeq[ActorRef[Guidebook.Message]]
  ): Behavior[Tourist.Message] =
    Behaviors.receive { (context, message) =>
      message match {
        case GuidebookUpdated(newGuidebooks) =>
          context.log.info(s"newGuidebooks: $newGuidebooks")
          receive(newGuidebooks.toIndexedSeq)
        case Start(codes) =>
          if (guidebooks.nonEmpty) {
            val guidebook = guidebooks(random(guidebooks.length))
            codes.foreach(guidebook ! Guidebook.Inquiry(_, context.self))
          } else {
            context.log.info("newGuidebooks is empty")
          }
          Behaviors.same
        case Guidance(code, description) =>
          println(s"$code, $description")
          Behaviors.same
      }
    }
}
