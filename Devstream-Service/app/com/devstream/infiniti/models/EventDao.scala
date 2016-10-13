package com.devstream.infiniti.models

import com.devstream.infiniti.Global
import com.devstream.infiniti.models.EventDao.ScrollType.ScrollType
import org.elasticsearch.index.query.{QueryBuilders, RangeQueryBuilder}
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.json.{JsArray, Json}


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
    asJsHitsArray(getEventsQuery.setQuery(getRangeQuery(before, ScrollType.Before)).get().toString)


  def getEventsQuery = esClient.prepareSearch("devstream")
    .setTypes("githubEvents", "stackoverflowEvents")
    .addSort("createdAt", SortOrder.DESC).setSize(30)

  def getEventsAfter(after: Long) =
    asJsHitsArray(getEventsQuery.setQuery(getRangeQuery(after, ScrollType.After)).get().toString)


  def asJsHitsArray(searchResponse: String) =
    JsArray(Json.parse(searchResponse) \\ "_source").toString()


  def getUserEvents(userId: String, before: Long) = {
    asJsHitsArray(esClient.prepareSearch("devstream").setTypes("githubEvents", "stackoverflowEvents")
      .setQuery(QueryBuilders.boolQuery().filter(QueryBuilders.matchQuery("user.employeeId", userId.toLowerCase))
        .filter(getRangeQuery(before, ScrollType.Before))).setSize(30).addSort("createdAt", SortOrder.DESC).get().toString)
  }

  private def getRangeQuery(timeStamp: Long, scrollType: ScrollType): RangeQueryBuilder = {
    if (scrollType == ScrollType.Before) {
      QueryBuilders.rangeQuery("createdAt").lte(timeStamp)
    } else {
      QueryBuilders.rangeQuery("createdAt").gte(timeStamp)
    }
  }
}
