package com.devstream.infiniti.utils

import org.elasticsearch.action.search.SearchResponse
import play.api.libs.json.{JsArray, JsObject, JsString, Json}

/**
  * Created by keerathj on 17/10/16.
  */

object Implicits {

  implicit class RichSearchResponse(searchResponse: SearchResponse) {

    def asJsHitsArray() =
      JsArray(Json.parse(searchResponse.toString) \\ "_source")

    def asJsAggsKeyArray() =
      JsArray(Json.parse(searchResponse.toString) \\ "key")

    def asJsAggsKeyAndCountArray() = {
      val buckets = (Json.parse(searchResponse.toString) \\ "buckets").head.as[JsArray]
      JsArray(buckets.value.map { bucketObj =>
        JsObject(Seq("timestamp" -> (bucketObj \ "key_as_string").get,
          "count" -> (bucketObj \ "doc_count").get))
      })
    }
  }

}
