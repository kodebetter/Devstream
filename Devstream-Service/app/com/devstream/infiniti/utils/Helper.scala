package com.devstream.infiniti.utils

import org.joda.time.DateTime

/**
  * Created by sandeept on 13/4/16.
  */
object Helper {

  def getMonthAndYear(date: DateTime): java.lang.Integer = {
    s"${date.getMonthOfYear}${date.getYear}".toInt
  }

}
