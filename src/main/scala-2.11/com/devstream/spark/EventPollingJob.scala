package com.devstream.spark

import com.devstream.provider.base.DevStreamEvent
import com.devstream.provider.github.ImplicitPayloadHandlers._
import com.devstream.provider.github.{Event, EventPayload, GithubPoller, Repo, User}
import org.apache.spark.{SparkConf, SparkContext}
import org.eclipse.egit.github.core.event.{Event => GhEvent, EventRepository => GhEventRepository, IssuesPayload => GhIssuePayload, PullRequestPayload => GhPullRequestPayload, PushPayload => GhPushPayload}
import org.eclipse.egit.github.core.{User => GhUser}

case class Provider(name: String, userName: String, authToken: String, lastSeenId: String)

case class DevStreamUser(providers: List[Provider])

object EventPollingJob {

  def main(args: Array[String]) = {
    val conf = new SparkConf()
    val sc = new SparkContext(conf)
    val providers = listUsers().flatMap(_.providers)
    val providersRDD = sc.parallelize(providers)

    providersRDD.foreach { provider =>
      provider.name match {
        case "github" => val events = GithubPoller.pollForUserEvents(provider.userName,
          provider.authToken, provider.lastSeenId)

          events.flatMap { event =>
            transformEvent(event)
          }.foreach { event =>
            
          }
      }
    }
  }

  def listUsers(): List[DevStreamUser] = List(DevStreamUser(List(Provider("", "", "", ""))))

  def transformEvent(event: GhEvent) = {
    event.getPayload match {
      case prPayload: GhPullRequestPayload =>
        Some(transformToDevStreamEvent(event, prPayload.transformPayload))
      case issuePayload: GhIssuePayload =>
        Some(transformToDevStreamEvent(event, issuePayload.transformPayload))
      case pushPayload: GhPushPayload =>
        Some(transformToDevStreamEvent(event, pushPayload.transformPayload))
      case _ => None
    }
  }

  def transformToDevStreamEvent(ghEvent: GhEvent, payload: EventPayload) = {
    val createdAt = ghEvent.getCreatedAt.getTime / 1000L
    val id = ghEvent.getId
    val `type` = ghEvent.getType
    val repo = extractRepo(ghEvent.getRepo)
    val user = extractUser(ghEvent.getActor)
    val event = Event(id, user, repo, payload)
    DevStreamEvent(provider = "github", `type`, event, createdAt)
  }

  def extractRepo(eventRepository: GhEventRepository): Repo = {
    val id = eventRepository.getId
    val name = eventRepository.getName
    val url = eventRepository.getUrl
    Repo(id, name, url)
  }

  def extractUser(user: GhUser): User = {
    val id = user.getId
    val login = user.getLogin
    val url = user.getUrl
    val imageUrl = user.getAvatarUrl
    User(id, login, url, imageUrl)
  }
}
