package com.devstream.infiniti.utils

import org.joda.time.DateTime
import play.api.libs.json.{JsObject, JsValue}

import scala.xml.{Elem, Node, Null, Text}

/**
  * Created by sandeept on 13/4/16.
  */
object Helper {
  def trimXML = scala.xml.Utility.trim _
  
  def getMonthAndYear(date: DateTime): java.lang.Integer = {
    s"${date.getMonthOfYear}${date.getYear}".toInt
  }


  def transformXML(xml: Node, elemName: String): Map[String, String] = {
    val mayBeMetadata = (xml \\ elemName).headOption
    mayBeMetadata match {
      case Some(md) =>
        val metadataElem = Helper.trimXML(md).child
        val metadata = metadataElem.map {
          case Elem(prefix, label, attribs, scope, Text(text)) => label -> text
          case Elem(null, label, Null, scope) => label -> ""
        }.toMap
        metadata
      case None => Map.empty
    }
  }

  def transformJson(json: JsValue): Map[String, AnyRef] = {
    json.as[JsObject].fields.toMap

  }

}
