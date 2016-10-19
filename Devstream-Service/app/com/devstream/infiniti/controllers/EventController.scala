package com.devstream.infiniti.controllers

import com.devstream.infiniti.models.EventDao
import play.api.mvc.{Action, Controller}

/**
  * Created by sandeept on 12/10/16.
  */
object EventController extends Controller {

  def getUserEvents(userId: String, before: Option[String], after: Option[String]) = Action {
    implicit request =>
      val response = if (before.isDefined) {
        EventDao.getUserEventsBefore(userId, timeStampAsLong(before))
      } else {
        EventDao.getUserEventsAfter(userId, timeStampAsLong(after))
      }
      Ok(response).as("application/json")
  }

  def getEvents(before: Option[String], after: Option[String]) = Action {
    implicit request =>
      val response = if (before.isDefined) {
        EventDao.getEventsBefore(timeStampAsLong(before))
      } else {
        EventDao.getEventsAfter(timeStampAsLong(after))
      }
      Ok(response).as("application/json")
  }

  private def timeStampAsLong(timeStamp: Option[String]) =
    timeStamp.map(_.toLong).getOrElse(System.currentTimeMillis() / 1000L)

}
