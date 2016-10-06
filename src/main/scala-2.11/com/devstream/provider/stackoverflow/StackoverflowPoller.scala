package com.devstream.provider.stackoverflow

import com.devstream.apiclients.stackexchange.api.StackexchangeApiPackage
import com.devstream.apiclients.stackexchange.request.StackexchangeSite
import com.devstream.apiclients.stackexchange.request.StackexchangeSite.StackexchangeSite
import com.devstream.apiclients.stackexchange.response.UserTimelineBase
import com.devstream.log.Logger
import com.devstream.provider.base.Poller
import scala.collection.mutable


/**
  * Created by bipulk on 10/6/16.
  */

object StackoverflowPoller extends Poller with Logger {

  // change this with your
  private val applicationKey = "U4DMV*8nvpm3EOpvf69Rxw(("
  private val site: StackexchangeSite = StackexchangeSite.stackoverflow

  // keep page size to 100 for production use.
  private val pageSize = 100

  def pollForUserTimeline(token: String, lastSeenTimeStamp: Long): List[UserTimelineBase] = {

    recursiveCall(applicationKey, token, site, 1, pageSize, lastSeenTimeStamp)

  }


  private def recursiveCall(key: String, accessToken: String, site: StackexchangeSite, pageNo: Int, pageSize: Int, lastSeenTimeStamp: Long): List[UserTimelineBase] = {

    val currentPage = StackexchangeApiPackage.getMyTimeline(applicationKey, accessToken, site, pageNo, pageSize)

    val userTimelineBaseLastEntry: UserTimelineBase = currentPage.items(currentPage.items.size - 1).asInstanceOf[UserTimelineBase]

    if (currentPage.hasMore && userTimelineBaseLastEntry.timeStamp > lastSeenTimeStamp) {

      currentPage.items.filter(_.asInstanceOf[UserTimelineBase].timeStamp > lastSeenTimeStamp).asInstanceOf[List[UserTimelineBase]] :::
        recursiveCall(key, accessToken, site, pageNo + 1, pageSize, lastSeenTimeStamp)

    } else {

      currentPage.items.filter(_.asInstanceOf[UserTimelineBase].timeStamp > lastSeenTimeStamp).asInstanceOf[List[UserTimelineBase]]

    }

  }

  def main(args: Array[String]) {

    val list = pollForUserTimeline("89hKyjDW3MIHIvKGt92g6A))", 1417516662)

    println(list)
  }

}
