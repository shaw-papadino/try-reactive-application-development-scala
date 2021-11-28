package com.rarebooks.library

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{
  ActorContext,
  Behaviors,
  StashBuffer,
  TimerScheduler
}

import java.util.concurrent._
import scala.concurrent.duration._

object RareBooks {

  sealed trait InternalMessage extends RareBooksProtocol.Message

  final case object Open extends InternalMessage

  final case object Close extends InternalMessage

  final case object Report extends InternalMessage

  def apply(): Behavior[RareBooksProtocol.Message] = {
    Behaviors.withTimers { timers =>
      Behaviors.withStash(100) { buffer =>
        Behaviors.setup { context =>
          RareBooks(context, buffer, timers).openingWork()
        }
      }
    }
  }
}

case class RareBooks(context: ActorContext[RareBooksProtocol.Message],
                     buffer: StashBuffer[RareBooksProtocol.Message],
                     timers: TimerScheduler[RareBooksProtocol.Message]) {
  import RareBooks._

  protected def createLibrarian: ActorRef[RareBooksProtocol.Message] = {
    context.spawn(Librarian(findBookDuration), "librarian")
  }
  private val openDuration: FiniteDuration =
    Duration(
      context.system.settings.config
        .getDuration("rare-books.open-duration", SECONDS),
      SECONDS
    )

  private val closeDuration: FiniteDuration =
    Duration(
      context.system.settings.config
        .getDuration("rare-books.close-duration", SECONDS),
      SECONDS
    )

  private val findBookDuration: FiniteDuration =
    Duration(
      context.system.settings.config
        .getDuration("rare-books.librarian.find-book-duration", SECONDS),
      SECONDS
    )

  def openingWork(): Behavior[RareBooksProtocol.Message] = {
    timers.startSingleTimer(Close, closeDuration)
    open(librarian = createLibrarian)
  }
  def open(librarian: ActorRef[RareBooksProtocol.Message],
           requestsToday: Int = 0,
           totalRequests: Int = 0): Behavior[RareBooksProtocol.Message] =
    Behaviors.receive { (context, message) =>
      Behaviors.withTimers { timers =>
        message match {
          case Close =>
            timers.startSingleTimer(Open, openDuration)
            context.log.info("Closing down for the day.")
            context.self ! Report
            closed(librarian, requestsToday, totalRequests)
          case message =>
            context.log.info(message.toString)
            librarian ! message
            open(librarian, requestsToday + 1, totalRequests)
        }
      }
    }

  def closed(librarian: ActorRef[RareBooksProtocol.Message],
             requestsToday: Int,
             totalRequests: Int): Behavior[RareBooksProtocol.Message] =
    Behaviors.receive { (context, message) =>
      Behaviors.withTimers { timers =>
        message match {
          case Report =>
            val updatedTotalRequests = totalRequests + requestsToday
            context.log.info(
              s"$requestsToday requests processed today. Total requests processed = $updatedTotalRequests"
            )
            closed(librarian, requestsToday = 0, updatedTotalRequests)
          case Open =>
            timers.startSingleTimer(RareBooks.Close, closeDuration)
            context.log.info("Time to open up!")
            buffer.unstashAll(open(librarian, requestsToday, totalRequests))
          case other =>
            buffer.stash(other)
            Behaviors.same

        }
      }
    }
}
