package com.devstream.utils

import java.net.InetAddress

import com.devstream.model.{DevStreamEvent, DevStreamUser}
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.script.Script
import org.elasticsearch.script.ScriptService.ScriptType
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization._

import scala.collection.JavaConverters._

object EsUtils {

  implicit val formats = Serialization.formats(NoTypeHints)

  private val esClient = buildClient

  private def buildClient = {
    val settings = Settings.settingsBuilder()
      .put("cluster.providerName", "elasticsearch")
      .put("client.transport.sniff", true).build()

    TransportClient.builder().settings(settings).build
      .addTransportAddress(new InetSocketTransportAddress(
        InetAddress.getByName("localhost"), 9300))
  }

  def getUserIds: List[String] = esClient.prepareSearch("devstream").setTypes("user")
    .setSize(1000).setNoFields().get().getHits.getHits.map(_.getId).toList

  def getDevStreamUser(id: String): Option[DevStreamUser] = esClient.prepareSearch().setTypes("user")
    .setQuery(QueryBuilders.idsQuery("user").ids(id)).get().getHits.getHits.headOption
    .map(hit => read[DevStreamUser](hit.sourceAsString()))

  def updateLastSeen(id: String, providerName: String, lastSeen: String) = {
    val params = Map("providerName" -> providerName.toLowerCase, "lastSeen" -> lastSeen)
    val script = """ctx._source.profiles.find {profile -> profile.providerName == providerName}.lastSeen = lastSeen"""
    val lastSeenUpdateScript = new Script(script, ScriptType.INLINE, null, params.asJava)

    esClient.prepareUpdate("devstream", "user", id).setType("user").setScript(lastSeenUpdateScript).get().isCreated
  }


  def bulkInsert(providerName: String, events: List[DevStreamEvent]): Unit = {
    val bulkRequest = esClient.prepareBulk()
    events.foreach { event =>
      bulkRequest.add(esClient.prepareIndex("devstream", s"${providerName.toLowerCase}Events")
        .setCreate(false).setSource(write(event)))
    }
    val bulkResponse = bulkRequest.get()
    if (bulkResponse.hasFailures) {
      println(bulkResponse.buildFailureMessage())
    }
  }
}