package com.devstream.poll

import com.devstream.converter.GitHubEventConverters._
import com.devstream.log.Logger
import com.devstream.model.{DevStreamEvent, DevStreamUser, Profile}
import org.eclipse.egit.github.core.client.{GitHubClient, PageIterator}
import org.eclipse.egit.github.core.event.{Event => GhEvent}
import org.eclipse.egit.github.core.service.EventService

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.util.control._
import scala.util.{Failure, Success, Try}

object GithubPoller extends Poller with Logger {

  private def pageIteratorOfEvents(profile: Profile): PageIterator[GhEvent] = {
    val client = new GitHubClient()
    client.setOAuth2Token(profile.authToken)
    val eventService = new EventService(client)
    val isPublic = true
    eventService.pageUserEvents(profile.payload("login").asInstanceOf[String], isPublic, 1, 30)
  }

  override def pollForEvents(user: DevStreamUser, profile: Profile): List[DevStreamEvent] = {
    val events = pollForEvents(profile)
    val maybeLastSeenId = events.headOption.map(_.getId)
    if (maybeLastSeenId.isDefined) {
      updateLastSeen(user.employeeId, "github", maybeLastSeenId.get)
    }
    events.flatMap(_.asDevStreamEvent(user))
  }

  private def pollForEvents(profile: Profile): List[GhEvent] = {
    val newEvents = ListBuffer[GhEvent]()
    val pageIterator = pageIteratorOfEvents(profile)
    val userName = profile.payload("login")

    Breaks.breakable {
      var pageCount = 1
      while (pageIterator.hasNext) {
        Try(pageIterator.next()) match {
          case Success(events) =>
            log.info(s"Processing Page $pageCount for github user $userName")
            pageCount += 1

            val maybeLastSenEvent = events.zipWithIndex.find(_._1.getId == profile.lastSeen)
            if (maybeLastSenEvent.isDefined) {
              newEvents ++= events.take(maybeLastSenEvent.get._2)
              Breaks.break()
            } else {
              newEvents ++= events
            }

          case Failure(throwable) => throw new Exception(throwable)
        }
      }
      log.info(s"Processed $pageCount pages for github user $userName")
    }
    newEvents.toList.sortWith((x,y) => x.getCreatedAt.getTime > y.getCreatedAt.getTime)
  }
}

