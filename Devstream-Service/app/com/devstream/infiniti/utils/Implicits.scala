package com.devstream.infiniti.utils

import org.elasticsearch.action.search.SearchResponse
import play.api.libs.json.{JsArray, Json}

/**
  * Created by keerathj on 17/10/16.
  */

object Implicits {

  implicit class RichSearchResponse(searchResponse: SearchResponse) {

    def asJsHitsArray() =
      JsArray(Json.parse(searchResponse.toString) \\ "_source")
  }
}
