package com.devstream.infiniti.models

import com.devstream.infiniti.Global
import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.script.Script
import org.elasticsearch.script.ScriptService.ScriptType
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval
import com.devstream.infiniti.utils.Implicits._

/**
  * Created by sandeept on 12/10/16.
  */
object UserDao {

  private val esClient = Global.getEsClient


  def insert(jsonString: String, id: String) = {
    esClient.prepareIndex("devstream", "user", id).setCreate(false).setSource(jsonString).get()
  }

  def queryForUser(userId: String): Option[String] = {
    val response = esClient.prepareSearch("devstream").setQuery(QueryBuilders.idsQuery("user").addIds(userId)).get().getHits.getHits.headOption.map(_.sourceAsString())
    response
  }

  def delete(providerName: String, userId: String) = {

    val params = Map("providerName" -> providerName.toLowerCase)
    import scala.collection.JavaConverters._

    val script = new Script("ctx._source.profiles -= ctx._source.profiles.find {profile -> profile.providerName == providerName}",
      ScriptType.INLINE, null, params.asJava)
    esClient.prepareUpdate("devstream", "user", userId).setScript(script).get()
  }

  def update(profileJson: String, id: String) = {

    val jsonMap = new ObjectMapper().readValue(profileJson, classOf[java.util.HashMap[String, Object]])
    val params = Map("profile" -> jsonMap)

    import scala.collection.JavaConverters._
    val script = new Script(s"ctx._source.profiles += profile", ScriptType.INLINE, null, params.asJava)
    esClient.prepareUpdate("devstream", "user", id).setScript(script).setFields("_source").get().getGetResult.sourceAsString()
  }

  def queryForUserPunchCard(employeeId: String) = {

    def boolQueryWithIdAndRangeFilters() = {
      val boolQuery = QueryBuilders.boolQuery()
      boolQuery.filter(QueryBuilders.matchQuery("user.employeeId", employeeId))
      boolQuery.filter(QueryBuilders.rangeQuery("createdAt").lte("now/d").gt("now-1y/d"))
      boolQuery
    }

    esClient.prepareSearch("devstream").setTypes("githubEvents", "stackoverflowEvents")
      .setQuery(boolQueryWithIdAndRangeFilters())
      .addAggregation(AggregationBuilders.dateHistogram("activity_over_time").field("createdAt")
        .interval(DateHistogramInterval.DAY).format("dd-MM-yyyy")).get()
      .asJsAggsKeyStringAndCountArray("date").toString()
  }
}
