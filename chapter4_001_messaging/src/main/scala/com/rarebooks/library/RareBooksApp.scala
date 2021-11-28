package com.rarebooks.library

import akka.NotUsed
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object RareBooksApp extends App {

  def guardian: Behavior[NotUsed] = Behaviors.setup { context =>
    val rareBook = context.spawn(RareBooks(), "rarebook")
    val customer = context.spawn(Customer(rareBook, 80, 5), "customer")
    Behaviors.empty
  }
  ActorSystem(guardian, "RareBooksSystem")
}
