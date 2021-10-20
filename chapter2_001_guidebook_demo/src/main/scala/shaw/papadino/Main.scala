//#full-example
package shaw.papadino

import java.util.Locale

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

//#main-class
object Main extends App {
  def guardian: Behavior[NotUsed] = Behaviors.setup { context =>
    val guideBook = context.spawn(Guidebook.receive(), "guidebook")
    val tourist = context.spawn(Tourist(guideBook.ref), "tourist")

    tourist ! Tourist.Start(Locale.getISOCountries)

    Behaviors.empty
  }
  ActorSystem(guardian, "GuideSystem")
}
