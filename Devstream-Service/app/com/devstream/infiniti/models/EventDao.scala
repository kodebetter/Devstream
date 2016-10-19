package com.devstream.infiniti.models

import com.devstream.infiniti.Global
import com.devstream.infiniti.models.EventDao.ScrollType.ScrollType
import org.elasticsearch.index.query.{MatchQueryBuilder, QueryBuilders, RangeQueryBuilder}
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.json.{JsArray, Json}
import com.devstream.infiniti.utils.Implicits._

/**
  * Created by sandeept on 12/10/16.
  */

object EventDao {

  object ScrollType extends Enumeration {
    type ScrollType = Value
    val Before, After = Value
  }

  val esClient = Global.getEsClient

  def getEventsBefore(before: Long) =
    getEventsQuery.setQuery(getRangeQuery(before, ScrollType.Before)).get().asJsHitsArray().toString()


  private def getEventsQuery = esClient.prepareSearch("devstream")
    .setTypes("githubEvents", "stackoverflowEvents")
    .addSort("createdAt", SortOrder.DESC).setSize(30)

  def getEventsAfter(after: Long) =
    getEventsQuery.setQuery(getRangeQuery(after, ScrollType.After)).get().asJsHitsArray().toString()

  private def getUserMatchFilter(userId: String) = {
    val userMatchQuery = QueryBuilders.matchQuery("user.employeeId", userId.toLowerCase)
    QueryBuilders.boolQuery().filter(userMatchQuery)
  }

  def getUserEventsBefore(userId: String, before: Long) =
    getEventsQuery.setQuery(getUserMatchFilter(userId)
      .filter(getRangeQuery(before, ScrollType.Before))).get().asJsHitsArray().toString()

  def getUserEventsAfter(userId: String, after: Long) =
    getEventsQuery.setQuery(getUserMatchFilter(userId)
      .filter(getRangeQuery(after, ScrollType.After))).get().asJsHitsArray().toString()


  private def getRangeQuery(timeStamp: Long, scrollType: ScrollType): RangeQueryBuilder = {
    if (scrollType == ScrollType.Before) {
      QueryBuilders.rangeQuery("createdAt").lte(timeStamp)
    } else {
      QueryBuilders.rangeQuery("createdAt").gte(timeStamp)
    }
  }
}
