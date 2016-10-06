package com.devstream.provider.github

import com.devstream.log.Logger
import com.devstream.provider.base.Poller
import org.eclipse.egit.github.core.client.{GitHubClient, PageIterator}
import org.eclipse.egit.github.core.event.{Event => GhEvent}
import org.eclipse.egit.github.core.service.EventService

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.util.control._
import scala.util.{Failure, Success, Try}

object GithubPoller extends Poller with Logger {

  def pollForUserEvents(user: String, accessToken: String,
                        lastSeenId: String): List[GhEvent] = {

    def pageIteratorOfEvents(): PageIterator[GhEvent] = {
      val client = new GitHubClient()
      client.setOAuth2Token(accessToken)
      val eventService = new EventService(client)
      val isPublic = true
      eventService.pageUserEvents(user, isPublic, 1, 30)
    }

    val newEvents = ListBuffer[GhEvent]()
    val pageIterator = pageIteratorOfEvents()

    Breaks.breakable {
      var pageCount = 0
      while (pageIterator.hasNext) {
        Try(pageIterator.next()) match {
          case Success(events) => pageCount += 1
            if (pageCount == 1) {
              val newLastSeenId = events.head.getId
              println(newLastSeenId) // post the new last seen id to the server for that user
            }

            log.info(s"Processing Page $pageCount for github user $user")
            val maybeLastSenEvent = events.zipWithIndex.find(_._1.getId == lastSeenId)

            if (maybeLastSenEvent.isDefined) {
              newEvents ++= events.take(maybeLastSenEvent.get._2)
              Breaks.break()
            } else {
              newEvents ++= events
            }

          case Failure(throwable) => throw new Exception(throwable)
        }
      }
      log.info(s"Processed $pageCount pages for github user $user")
    }
    newEvents.toList
  }

  println(pollForUserEvents("keerath",
    "69687d92b66666618e3d88adb5afe7c913ad29fc",
    "4580221305"))
}