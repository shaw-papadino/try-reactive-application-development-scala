//#full-example
package shaw.papadino

import java.util.Locale

import akka.NotUsed
import akka.actor.typed.{ActorSystem, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.{Behaviors, Routers}
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
    (0 to 10).foreach { n =>
      context.spawn(Guidebook(), s"guidebook-pool-$n")
    }

    Behaviors.empty
  }
  val system = ActorSystem(guardian, "ClusterSystem", config)
  implicit val ec = system.executionContext
}
