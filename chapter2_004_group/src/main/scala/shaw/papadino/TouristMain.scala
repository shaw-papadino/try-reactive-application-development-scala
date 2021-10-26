//#full-example
package shaw.papadino

import akka.NotUsed
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory

object TouristMain extends App {
  val config = ConfigFactory
    .parseString(s"""
        akka.remote.artery.canonical.port=25252
        akka.cluster.roles = [tourist]
        """)
    .withFallback(ConfigFactory.load("application"))

  def guardian: Behavior[NotUsed] = Behaviors.setup { context =>
    Cluster(context.system)
    context.spawn(Tourist(), "tourist")

    Behaviors.empty
  }
  ActorSystem(guardian, "ClusterSystem", config)
}
