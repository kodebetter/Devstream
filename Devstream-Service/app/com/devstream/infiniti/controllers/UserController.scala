package com.devstream.infiniti.controllers

import com.devstream.infiniti.Global
import com.devstream.infiniti.models.constants.DevStreamConstants
import com.devstream.infiniti.models.{EmployeeDetails, User, UserDao}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._
import play.api.Play.current
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.libs.ws.WS
import play.api.mvc.{Action, BodyParsers, Controller}
import play.api.{Logger, Play}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by sandeept on 6/4/16.
  */

case class Response(message: String, Status: String)

object UserController extends Controller {

  implicit val formats = Serialization.formats(NoTypeHints)

  val logger = Logger(getClass.getCanonicalName).logger
  val corridorUrl = Play.application.configuration.getString("CORRIDOR_URL").getOrElse("")
  val serviceUrl = Play.application.configuration.getString("SERVICE_URL").getOrElse("")
  val gitHubUrl = Play.application.configuration.getString("GITHUB_URL").getOrElse("")
  val stackOverFlowUrl = Play.application.configuration.getString("STACKOVERFLOW_URL").getOrElse("")

  def login(token: String) = Action {
    implicit request =>
      val result = WS.url(corridorUrl).withQueryString("ticket" -> token).withQueryString("service" -> serviceUrl).get()
      val response = Await.result(result, 5 seconds)
      response.status match {
        case 200 =>
          println("" + response.toString)
          val user = response.xml.head
          print("user " + user.toString())
          val data = (user \\ "authenticationSuccess").head
          val employeeDetails = User.extractDetails(data)
          println("metadata " + employeeDetails)
          val tokenList = UserDao.queryForUser(employeeDetails.employeeId)
          tokenList match {
            case Some(casResponse) =>
              Ok(casResponse).as("application/json")
            case None =>
              UserDao.insert(write[EmployeeDetails](employeeDetails), employeeDetails.employeeId)
              Ok(write[EmployeeDetails](employeeDetails)).as("application/json")

          }
        case _ =>
          logger.error(s"Invalid Request notification ")
          BadRequest
      }
  }


  def updateUser(userId: String) = Action(BodyParsers.parse.json) { implicit request =>
    val userDetails = User.update(request.body)
    val gitHubProfile = userDetails.profiles.find(_.providerName.toLowerCase == DevStreamConstants.github)
    val stackOverFlowProfile = userDetails.profiles.find(_.providerName.toLowerCase == DevStreamConstants.stackOverFlow)
    if (gitHubProfile.isDefined) {
      val result = WS.url(gitHubUrl).withQueryString("access_token" -> gitHubProfile.get.authtoken).get()
      val response = Await.result(result, 5 seconds)
      val str = JsObject(Seq("authToken" -> JsString(gitHubProfile.get.authtoken), "providerName" -> JsString(gitHubProfile.get.providerName),
        "payload" -> response.json, "lastSeen" -> JsString(gitHubProfile.get.lastSeen))).toString()
      val updatedResponse = UserDao.update(str, userId)
      Ok(updatedResponse).as("application/json")
    } else if (stackOverFlowProfile.isDefined) {
      val stackOverFlowKey = Global.getConfig.getString(DevStreamConstants.stackOverFlowKey).get
      val result = WS.url(stackOverFlowUrl).withQueryString("key" -> stackOverFlowKey).withQueryString("site" -> DevStreamConstants.stackOverFlow).
        withQueryString("access_token" -> stackOverFlowProfile.get.authtoken).get()
      println(result)
      val response = Await.result(result, 5 seconds)
      val str = JsObject(Seq("authToken" -> JsString(stackOverFlowProfile.get.authtoken), "providerName" -> JsString(stackOverFlowProfile.get.providerName),
        "payload" -> response.json, "lastSeen" -> JsString(stackOverFlowProfile.get.lastSeen))).toString()
      println("" + response.json)
      val updatedResponse = UserDao.update(str, userId)
      Ok(updatedResponse).as("application/json")
    } else {
      BadRequest
    }
  }

  def deleteUser(userId: String, providerName: String) = Action {
    implicit request =>
      UserDao.delete(providerName, userId)
      val response = Response("success", "200")
      val write = Json.writes[Response]
      Ok(write.writes(response))
  }

  def getUserPunchCard(userId: String, timeZone: String) = Action { implicit request =>
    val response = UserDao.queryForUserPunchCard(userId, timeZone)
    Ok(response).as("application/json")
  }
}
