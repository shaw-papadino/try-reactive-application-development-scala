//#full-example
package shaw.papadino

import java.util.Locale

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory

//#main-class
object GuidebookMain extends App {
  val config = ConfigFactory
    .parseString(s"""
        akka.remote.artery.canonical.port=25251
        akka.cluster.roles = [guidebook]
        """)
    .withFallback(ConfigFactory.load("application"))

  def guardian: Behavior[NotUsed] = Behaviors.setup { context =>
    Cluster(context.system)
    context.spawn(Guidebook.apply(), "guidebook")

    Behaviors.empty
  }
  val system = ActorSystem(guardian, "ClusterSystem", config)
  implicit val ec = system.executionContext
}
