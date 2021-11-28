package com.rarebooks.library

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.util.Random

object Customer {
  import RareBooksProtocol._

  /**
    * Customer model.
    *
    * @param odds the customer's odds of finding a book
    * @param tolerance the customer's tolerance for BookNotFound
    * @param found the number of books found
    * @param notFound the number of books not found
    */
  case class CustomerModel(odds: Int, tolerance: Int, found: Int, notFound: Int)

  private case class State(model: CustomerModel, timeInMillis: Long) {
    def update(m: Message) =
      m match {
        case BookFound(b, d) =>
          copy(model.copy(found = model.found + b.size), timeInMillis = d)
        case BookNotFound(_, d, _) =>
          copy(model.copy(notFound = model.notFound + 1), timeInMillis = d)
        case Credit(d) =>
          copy(model.copy(notFound = 0), timeInMillis = d)
      }
    def isOverTolerance: Boolean = model.tolerance <= model.notFound
  }

  def apply(bookStore: ActorRef[RareBooksProtocol.Message],
            odds: Int,
            tolerance: Int): Behavior[RareBooksProtocol.Message] = {
    Behaviors.setup { context =>
      Customer(bookStore, odds, tolerance, context).setup()
    }
  }

  case class Customer(bookStore: ActorRef[RareBooksProtocol.Message],
                      odds: Int,
                      tolerance: Int,
                      context: ActorContext[RareBooksProtocol.Message]) {
    import Customer._
    import RareBooksProtocol._

    def setup(): Behavior[RareBooksProtocol.Message] = {
      requestBookInfo(odds)
      receive(State(CustomerModel(odds, tolerance, 0, 0), -1L))
    }

    def receive(state: State): Behavior[RareBooksProtocol.Message] = {
      Behaviors.receiveMessage[RareBooksProtocol.Message] {
        case m: BookFound =>
          requestBookInfo(odds)
          context.log.info(s"${m.books.size} Book(s) found!")
          receive(state.update(m))
        case m: BookNotFound if state.isOverTolerance =>
          m.sender ! Complain(requester = context.self)
          context.log.info(
            s"${state.model.notFound} Book(s) not found! Reached my tolerance of ${state.model.tolerance}. Sent complaint!"
          )
          receive(state.update(m))
        case m: BookNotFound =>
          requestBookInfo(odds)
          context.log.info(
            s"${state.model.notFound} Book(s) not found! My tolerance is ${state.model.tolerance}"
          )
          receive(state.update(m))
        case m: Credit =>
          requestBookInfo(odds)
          context.log.info("Credit received, will start requesting again!")
          receive(state.update(m))
      }
    }

    private def requestBookInfo(odds: Int): Unit =
      bookStore ! FindBookByTopic(
        Set(pickTopic(odds)),
        requester = context.self
      )

    private def pickTopic(odds: Int): Topic =
      if (Random.nextInt(100) < odds)
        viableTopics(Random.nextInt(viableTopics.size))
      else Unknown
  }

}
