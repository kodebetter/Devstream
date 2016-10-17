package com.devstream.infiniti.controllers

import java.time.{Instant, ZoneId, ZonedDateTime}

import com.devstream.infiniti.models.TrendsDao
import play.api.mvc.{Action, Controller}

/**
  * Created by keerathj on 12/10/16.
  */

object TrendsController extends Controller {

  def getTrendingUsers(days: Option[Int]) = Action { implicit request =>
    val intervalEnd = startOfDay.plusDays(1)

    val intervalStart = if (days.isDefined) {
      intervalEnd.minusDays(days.get)
    } else {
      intervalEnd.minusDays(1)
    }

    val response = TrendsDao
      .aggregateUsersInTimePeriod(intervalStart.toEpochSecond, intervalEnd.toEpochSecond)
    Ok(response).as("application/json")
  }

  private def startOfDay = {
    val zoneId = ZoneId.of("UTC")
    val zonedDateTime = ZonedDateTime.ofInstant(Instant.now(), zoneId)
    zonedDateTime.toLocalDate.atStartOfDay(zoneId)
  }
}
