package com.devstream.spark

import com.devstream.model.DevStreamEvent
import com.devstream.poll.{GithubPoller, StackOverFlowPoller}
import com.devstream.utils.EsUtils
import org.apache.spark.{SparkConf, SparkContext}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization


object EventPollingJob {

  implicit val formats = Serialization.formats(NoTypeHints)

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local[4]")
    conf.setAppName("Test")
    val sc = new SparkContext(conf)
    val userIds = sc.parallelize(EsUtils.getUserIds)

    userIds.flatMap { userId =>
      EsUtils.getDevStreamUser(userId)
    }.foreach { user =>
      user.profiles.foreach { profile =>
        profile.providerName.toLowerCase match {
          case "github" =>
            val events = GithubPoller.pollForEvents(user, profile)
            bulkInsertIntoES("github", events)

          case "stackoverflow" =>
            val events = StackOverFlowPoller.pollForEvents(user, profile)
            bulkInsertIntoES("stackoverflow", events)
        }
      }
    }
  }

  def bulkInsertIntoES(providerName: String, events: List[DevStreamEvent]): Unit =
    if (events.nonEmpty) {
      EsUtils.bulkInsert(providerName, events)
    }
}
