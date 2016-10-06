package com.devstream.spark

import com.devstream.provider.base.DevStreamEvent
import com.devstream.provider.github.{Event, EventPayload, GithubPoller, Repo, User}
import org.eclipse.egit.github.core.{User => GhUser}
import org.apache.spark.SparkContext
import org.eclipse.egit.github.core.event.{Event => GhEvent, EventRepository => GhEventRepository, IssuesPayload => GhIssuePayload, PullRequestPayload => GhPullRequestPayload, PushPayload => GhPushPayload}

case class Provider(name: String, userName: String, authToken: String, lastSeenId: String)

case class DevStreamUser(providers: List[Provider])

object EventPollingJob {


  import com.devstream.provider.github.ImplicitPayloadHandlers._

  def transformEvent(event: GhEvent) = {

    val maybePayload = event.getPayload match {
      case prPayload: GhPullRequestPayload =>
        Some(prPayload.transformPayload)
      case issuePayload: GhIssuePayload =>
        Some(issuePayload.transformPayload)
      case pushPayload: GhPushPayload =>
        Some(pushPayload.transformPayload)
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

  def extractUser(user: GhUser): User = {
    val id = user.getId
    val login = user.getLogin
    val url = user.getUrl
    val imageUrl = user.getAvatarUrl
    User(id, login, url, imageUrl)
  }

  def extractRepo(eventRepository: GhEventRepository): Repo = {
    val id = eventRepository.getId
    val name = eventRepository.getName
    val url = eventRepository.getUrl
    Repo(id, name, url)
  }
}

