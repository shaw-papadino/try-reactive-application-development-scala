package shaw.papadino

import java.util.{Currency, Locale}

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Guidebook {
  sealed trait Message
  case class Inquiry(code: String, replyTo: ActorRef[Tourist.Message])
      extends Message

  def describe(locale: Locale): String =
    s"""In ${locale.getDisplayCountry},
       |${locale.getDisplayLanguage} is spoken and the currency
       |is the ${Currency.getInstance(locale).getDisplayName}
       |""".stripMargin

  def receive(): Behavior[Guidebook.Message] = {
    Behaviors.receiveMessage[Guidebook.Message] {
      case Inquiry(code, replyTo) =>
        Locale.getAvailableLocales
          .filter(_.getCountry == code)
          .foreach { locale =>
            replyTo ! Tourist.Guidance(code, describe(locale))
          }
        Behaviors.same

    }
  }
}
