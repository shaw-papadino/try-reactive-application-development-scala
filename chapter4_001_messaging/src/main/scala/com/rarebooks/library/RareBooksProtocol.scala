package com.rarebooks.library

import akka.actor.typed.ActorRef

object RareBooksProtocol {

  sealed trait Topic
  case object Africa extends Topic
  case object Asia extends Topic
  case object Gilgamesh extends Topic
  case object Greece extends Topic
  case object Persia extends Topic
  case object Philosophy extends Topic
  case object Royalty extends Topic
  case object Tradition extends Topic
  case object Unknown extends Topic

  val viableTopics: List[Topic] =
    List(
      Africa,
      Asia,
      Gilgamesh,
      Greece,
      Persia,
      Philosophy,
      Royalty,
      Tradition
    )

  trait Message
  trait GeneralMessage extends Message {
    def dateInMillis: Long
  }

  final case class FindBookByIsbn(isbn: String,
                                  dateInMillis: Long =
                                    java.lang.System.currentTimeMillis(),
                                  requester: ActorRef[Message])
      extends GeneralMessage {
    require(isbn.nonEmpty, "Isbn required.")
  }
  final case class FindBookByTopic(topic: Set[Topic],
                                   dateInMillis: Long =
                                     java.lang.System.currentTimeMillis(),
                                   requester: ActorRef[Message])
      extends GeneralMessage {
    require(topic.nonEmpty, "Isbn required.")
  }
  final case class BookCard(isbn: String,
                            author: String,
                            title: String,
                            description: String,
                            dateOfOrigin: String,
                            topic: Set[Topic],
                            publisher: String,
                            language: String,
                            pages: Int)

  /**
    * List of book cards found message.
    *
    * @param books list of book cards
    * @param dateInMillis date message was created
    */
  final case class BookFound(books: List[BookCard],
                             dateInMillis: Long =
                               java.lang.System.currentTimeMillis())
      extends GeneralMessage {
    require(books.nonEmpty, "Book(s) required.")
  }

  /**
    * Book was not found message.
    *
    * @param reason reason book was not found
    * @param dateInMillis date message was created
    */
  final case class BookNotFound(reason: String,
                                dateInMillis: Long =
                                  java.lang.System.currentTimeMillis(),
                                sender: ActorRef[RareBooksProtocol.Message])
      extends GeneralMessage {
    require(reason.nonEmpty, "Reason is required.")
  }

  final case class Complain(requester: ActorRef[Message],
                            dateInMillis: Long =
                              java.lang.System.currentTimeMillis())
      extends GeneralMessage

  final case class Credit(
    dateInMillis: Long = java.lang.System.currentTimeMillis()
  ) extends GeneralMessage

}
