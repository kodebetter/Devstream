package com.devstream.infiniti.controllers

import com.devstream.infiniti.models.User
import play.api.libs.ws.WS
import play.api.mvc.{Action, BodyParsers, Controller}
import play.api.{Logger, Play}

import scala.util.{Failure, Success}


/**
  * Created by sandeept on 6/4/16.
  */

object UserController extends Controller {

  val logger = Logger(getClass.getCanonicalName).logger
  val corridorUrl = Play.application.configuration.getString("CORRIDOR_URL").getOrElse("")
  val serviceUrl = Play.application.configuration.getString("SERVICE_URL").getOrElse("")

  def register(tokenString: String) = Action {
    implicit request =>
      WS.url(corridorUrl).withQueryString("ticket" -> tokenString).withQueryString("service" -> serviceUrl).get().onComplete {
        case Success(resp) =>
          val user = resp.xml.head
          val name = (user \\ "name").text
          val lastName: String = (user \\ "lastName").text
          val email = (user \\ "email").text
          val password = (user \\ "password").text
          val userType = (user \\ "userType").text
          val mayBeMetadata = (user \\ "metadata").headOption


        case Failure(ex) => logger.error(s"Invitation Error sending Email notification for", ex)
      }
      Ok(
        <result>
          <status>success</status>
          <meassage>name</meassage>
        </result>)
  }


  def updateUser(userId: String) = Action(BodyParsers.parse.json) {
    implicit request =>
      User.update(request.body)
      Ok(
        <result>
          <status>success</status>
          <meassage>name</meassage>
        </result>)
  }


  def deleteUser(customerId: String) = Action {
    implicit request =>
      BadRequest(
        <result>
          <status>failure</status>
          <meassage>Customer already Migrated / Invalid Customer</meassage>
        </result>)
  }
}
