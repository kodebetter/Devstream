package com.devstream.infiniti

import play.api
import play.api.Play.current
import play.api.mvc._
import play.api.{Configuration, Logger, Play}


object Global extends WithFilters() {


  private var appConfig: Configuration = null

  def getConfig: Configuration = {
    appConfig
  }

  lazy val internalToken: String = {
    appConfig.getString("internal_token").getOrElse("testmyapp")
  }

  override def onStart(app: api.Application): Unit = {
    appConfig = Play.application.configuration

  }

  private def invalidSession = Action {
    Results.Forbidden("<result><status>failure</status><message>Invalid session</message></result>")
  }

  private def notFound = Action {
    Results.NotFound
  }

  private def unauthorized = Action {
    Results.Unauthorized("<result><status>failure</status><message>Invalid security params</message></result>")
  }



  override def onRouteRequest(requestHeader: RequestHeader): Option[Handler] = {
    val path = requestHeader.path
    Logger.info(s"serving request: $path")
    val queryToken = requestHeader.queryString.get("token")
    queryToken match {
      case Some(qt) =>
        if (qt.size >= 1 && qt.head.equalsIgnoreCase(internalToken))
          super.onRouteRequest(requestHeader)
        else {
          Logger.warn(s"Invalid Internal Token: $qt")
          Some(unauthorized)
        }
      case None =>
        Logger.warn("Missing internal token")
        Some(unauthorized)
    }
    super.onRouteRequest(requestHeader)


  }

}
