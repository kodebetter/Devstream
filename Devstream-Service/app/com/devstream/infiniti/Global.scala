package com.devstream.infiniti

import java.net.InetAddress

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import play.api
import play.api.Play.current
import play.api.mvc._
import play.api.{Configuration, Logger, Play}


object Global extends WithFilters() {


  private var appConfig: Configuration = null
  private var esClient: TransportClient = null

  def getConfig: Configuration = {
    appConfig
  }

  def getEsClient: TransportClient = {
    esClient
  }

  lazy val internalToken: String = {
    appConfig.getString("internal_token").getOrElse("testmyapp")
  }

  private def buildClient = {
    val settings = Settings.settingsBuilder()
      .put("cluster.name", "elasticsearch")
      .put("client.transport.sniff", true).build()

    TransportClient.builder().settings(settings).build
      .addTransportAddress(new InetSocketTransportAddress(
        InetAddress.getByName(appConfig.getString("ELASTIC_ENGINE_HOST").get), 9300))

  }
  override def onStart(app: api.Application): Unit = {
    appConfig = Play.application.configuration
    esClient = buildClient
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
    /*val queryToken = requestHeader.queryString.get("token")
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
    }*/
    super.onRouteRequest(requestHeader)

  }

}
