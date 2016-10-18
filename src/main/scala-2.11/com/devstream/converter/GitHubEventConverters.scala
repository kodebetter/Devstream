package com.devstream.converter

import com.devstream.model.{Author, Commit, DevStreamEvent, DevStreamUser, Event, EventPayload, IssuePayload, PullRequestPayload, PushPayload, Repo, Stats, User}
import org.eclipse.egit.github.core.event.{Event => GhEvent, EventRepository => GhEventRepository, IssuesPayload => GhIssuePayload, PullRequestPayload => GhPullRequestPayload, PushPayload => GhPushPayload}
import org.eclipse.egit.github.core.{Commit => GhCommit, CommitUser => GhCommitUser, PullRequest => GhPullRequest, User => GhUser}

import scala.collection.JavaConversions._

object GitHubEventConverters {

  sealed implicit class RichGitHubEvent(event: GhEvent) extends EventConverters {

    override def asDevStreamEvent(user: DevStreamUser): Option[DevStreamEvent] =
      transformPayload.map(payload => toDevStreamEvent(event, payload, user))

    private def transformPayload: Option[EventPayload] = {
      event.getPayload match {
        case prPayload: GhPullRequestPayload => Some(prPayload.asEventPayload)
        case issuePayload: GhIssuePayload => Some(issuePayload.asEventPayload)
        case pushPayload: GhPushPayload => Some(pushPayload.asEventPayload)
        case _ => None
      }
    }

    private def toDevStreamEvent(ghEvent: GhEvent, payload: EventPayload,
                                 employeeDetails: DevStreamUser) = {
      val createdAt = ghEvent.getCreatedAt.getTime / 1000L
      val id = ghEvent.getId
      val `type` = ghEvent.getType
      val repo = extractRepo(ghEvent.getRepo)
      val user = extractUser(ghEvent.getActor)
      val event = Event(id, user, repo, payload)
      DevStreamEvent(provider = "github", `type`, event,
        createdAt, asUserPayload(employeeDetails))
    }

    private def extractRepo(eventRepository: GhEventRepository): Repo = {
      val id = eventRepository.getId
      val name = eventRepository.getName
      val url = eventRepository.getUrl
      Repo(id, name, url)
    }

    private def extractUser(user: GhUser): User = {
      val id = user.getId
      val login = user.getLogin
      val imageUrl = user.getAvatarUrl
      User(id, login, imageUrl)
    }
  }

  private sealed trait EventPayloadConverter {
    def asEventPayload: EventPayload
  }

  private sealed implicit class PullReqPayloadConverter(pullReqPayload: GhPullRequestPayload)
    extends EventPayloadConverter {

    override def asEventPayload: EventPayload = {
      val action = pullReqPayload.getAction
      val number = pullReqPayload.getNumber
      val pullRequest = pullReqPayload.getPullRequest
      val url = pullRequest.getHtmlUrl
      val title = pullRequest.getTitle
      val isMerged = pullRequest.isMerged
      val stats = extractStats(pullRequest)
      PullRequestPayload(action, number, url, title, isMerged, stats)
    }

    private def extractStats(pullRequest: GhPullRequest): Stats = {
      val additions = pullRequest.getAdditions
      val deletions = pullRequest.getDeletions
      val commits = pullRequest.getCommits
      Stats(commits, additions, deletions)
    }
  }

  private sealed implicit class IssuePayloadConverter(issuePayload: GhIssuePayload)
    extends EventPayloadConverter {

    override def asEventPayload: EventPayload = {
      val action = issuePayload.getAction
      val issue = issuePayload.getIssue
      val number = issue.getNumber
      val title = issue.getTitle
      val url = issue.getHtmlUrl
      IssuePayload(action, number, title, url)
    }
  }

  private sealed implicit class PushPayloadConverter(pushPayload: GhPushPayload)
    extends EventPayloadConverter {

    override def asEventPayload: EventPayload = {
      val ref = pushPayload.getRef
      val before = pushPayload.getBefore
      val head = pushPayload.getHead
      val commits = pushPayload.getCommits.map(transformCommit).toList
      PushPayload(ref, before, head, commits)
    }

    private def transformCommit(commit: GhCommit) =
      Commit(commit.getSha, commit.getMessage, transformAuthor(commit.getAuthor), commit.getUrl)

    private def transformAuthor(author: GhCommitUser) =
      Author(author.getName, author.getEmail)
  }

}