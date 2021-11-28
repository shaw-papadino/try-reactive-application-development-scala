package com.rarebooks.library

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{
  ActorContext,
  Behaviors,
  StashBuffer,
  TimerScheduler
}
import com.rarebooks.library.RareBooksProtocol.{
  BookCard,
  BookFound,
  BookNotFound,
  FindBookByIsbn
}

import scala.concurrent.duration.FiniteDuration

object Librarian {
  import RareBooksProtocol._

  sealed trait InternalMessage extends Message
  final case class Done(e: Either[BookNotFound, BookFound],
                        customer: ActorRef[RareBooksProtocol.Message])
      extends InternalMessage

  def apply(duration: FiniteDuration): Behavior[RareBooksProtocol.Message] = {
    Behaviors.withTimers { timers =>
      Behaviors.withStash(100) { buffer =>
        Behaviors.setup { context =>
          Librarian(context, buffer, timers, duration).setup()
        }
      }
    }
  }

}

case class Librarian(context: ActorContext[RareBooksProtocol.Message],
                     buffer: StashBuffer[RareBooksProtocol.Message],
                     timers: TimerScheduler[RareBooksProtocol.Message],
                     findBookDuration: FiniteDuration) {
  import Librarian._
  import RareBooksProtocol._
  import Catalog._

  def optToEither[T](
    v: T,
    f: T => Option[List[BookCard]]
  ): Either[BookNotFound, BookFound] = {
    f(v) match {
      case book: Some[List[BookCard]] => Right(BookFound(book.get))
      case _ =>
        Left(
          BookNotFound(s"Book(s) not found based on $v", sender = context.self)
        )
    }
  }
  def setup(): Behavior[RareBooksProtocol.Message] = {
    ready()
  }

  def ready(): Behavior[RareBooksProtocol.Message] = {
    Behaviors.receiveMessage[RareBooksProtocol.Message] {
      case FindBookByTopic(topic, _, sender) =>
        research(Done(optToEither[Set[Topic]](topic, findBookByTopic), sender))
      case FindBookByIsbn(isbn, _, sender) =>
        research(Done(optToEither(isbn, findBookByIsbn), sender))
      case Complain(sender, d) =>
        sender ! Credit(d)
        context.log.info(s"Credit issued to customer $sender()")
        Behaviors.same
    }
  }
  def busy(): Behavior[RareBooksProtocol.Message] = {
    Behaviors.receiveMessage[RareBooksProtocol.Message] {
      case Done(d, sender) => {
        d fold (f => {
          sender ! f
          context.log.info(f.toString)
        },
        s => sender ! s)
        buffer.unstashAll(ready())
      }
      case other =>
        buffer.stash(other)
        Behaviors.same

    }
  }
  def research(done: Done): Behavior[RareBooksProtocol.Message] = {
    timers.startSingleTimer(done, findBookDuration)
    busy()
  }
}
