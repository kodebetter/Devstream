package com.devstream.infiniti.models

import com.devstream.infiniti.Global
import com.devstream.infiniti.utils.Implicits._
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import play.api.libs.json.{JsArray, JsValue, Json}

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


  private def keys(searchResponse: String) =
    (Json.parse(searchResponse) \\ "key").map(_.as[String]).zipWithIndex.toMap

  private def timeRangeQuery(start: Long, end: Long) =
    QueryBuilders.rangeQuery("createdAt").gte(start).lt(end)
}
