package com.devstream.poll

import com.devstream.apiclients.stackexchange.api.StackexchangeApiPackage
import com.devstream.apiclients.stackexchange.request.StackexchangeSite
import com.devstream.apiclients.stackexchange.request.StackexchangeSite.StackexchangeSite
import com.devstream.apiclients.stackexchange.response.UserTimelineBase
import com.devstream.converter.StackOverFlowEventConverters._
import com.devstream.log.Logger
import com.devstream.model.{DevStreamEvent, DevStreamUser, Profile}

import scala.util.Try

object StackOverFlowPoller extends Poller with Logger {

  // change this with your
  val applicationKey = "mAxonwxRcbbCNSgousxBRg(("
  private val site: StackexchangeSite = StackexchangeSite.stackoverflow

  // keep page size to 100 for production use.
  private val pageSize = 100

  override def pollForEvents(user: DevStreamUser, profile: Profile): List[DevStreamEvent] = {
    val events = recursiveCall(applicationKey, profile.authToken, site, 1, pageSize,
      Try(profile.lastSeen.toLong).getOrElse(0L)).sortWith((x,y) => x.timeStamp > y.timeStamp)

    val maybeLastSeenEvent: Option[UserTimelineBase] = events.headOption
    if (maybeLastSeenEvent.isDefined) {
      updateLastSeen(user.employeeId, "stackoverflow", maybeLastSeenEvent.get.timeStamp + "")
    }
    events.flatMap(_.asDevStreamEvent(user))
  }

  private def recursiveCall(key: String, accessToken: String, site: StackexchangeSite,
                            pageNo: Int, pageSize: Int, lastSeenTimeStamp: Long): List[UserTimelineBase] = {

    val currentPage = StackexchangeApiPackage.getMyTimeline(applicationKey, accessToken, site, pageNo, pageSize)

    val userTimelineBaseLastEntry = currentPage.items.lastOption.asInstanceOf[Option[UserTimelineBase]]

    if (currentPage.hasMore && userTimelineBaseLastEntry.isDefined && userTimelineBaseLastEntry.get.timeStamp > lastSeenTimeStamp) {
      recursiveCall(key, accessToken, site, pageNo + 1, pageSize, lastSeenTimeStamp) ++:
        currentPage.items.filter(_.asInstanceOf[UserTimelineBase].timeStamp > lastSeenTimeStamp).asInstanceOf[List[UserTimelineBase]]
    } else {
      currentPage.items.filter(_.asInstanceOf[UserTimelineBase].timeStamp > lastSeenTimeStamp).asInstanceOf[List[UserTimelineBase]]
    }
  }
}
