package com.devstream.infiniti.models

import com.devstream.infiniti.Global
import com.devstream.infiniti.utils.Implicits._
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.script.Script
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.json4s.JsonAST.JString
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue, Json}

import scala.collection.JavaConverters._

/**
  * Created by keerathj on 12/10/16.
  */

object TrendsDao {

  val esClient = Global.getEsClient

  def aggregateUsersInTimePeriod(start: Long, end: Long): String = {

    val trendingUserIdsVsPos = keys(esClient.prepareSearch("devstream")
      .setTypes("githubEvents", "stackoverflowEvents")
      .setQuery(timeRangeQuery(start, end))
      .addAggregation(AggregationBuilders.terms("trendingUsers")
        .field("user.employeeId")).get().toString)

    def getPos(jsValue: JsValue) = {
      trendingUserIdsVsPos((jsValue \ "employeeId").get.as[String])
    }

    JsArray(esClient.prepareSearch("devstream").setTypes("user")
      .setQuery(QueryBuilders.idsQuery("user")
        .ids(trendingUserIdsVsPos.keys.asJavaCollection))
      .get().asJsHitsArray().value
      .sortWith((x, y) => getPos(x) < getPos(y))).toString()
  }

  def aggregateProjectsInTimePeriod(start: Long, end: Long): String = {
    val script = new Script("""doc['event.repo.id'].value + '#' + doc['event.repo.name.untouched'].value""")

    JsArray(esClient.prepareSearch("devstream").setTypes("githubEvents")
      .setQuery(timeRangeQuery(start, end))
      .addAggregation(AggregationBuilders.terms("trendingProjects").script(script))
      .get().asJsAggsArray().value.map(repoStr => repoStrToRepoObj(repoStr.as[String]))).toString()
  }

  private def repoStrToRepoObj(repoStr: String) = {
    val repoStrSplits = repoStr.split('#').map(JsString)
    println(repoStrSplits.toList)
    JsObject(Seq("id" -> repoStrSplits(0), "name" -> repoStrSplits(1)))
  }

  private def keys(searchResponse: String) =
    (Json.parse(searchResponse) \\ "key").map(_.as[String]).zipWithIndex.toMap

  private def timeRangeQuery(start: Long, end: Long) =
    QueryBuilders.rangeQuery("createdAt").gte(start).lt(end)
}
