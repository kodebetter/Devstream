package com.devstream.provider.github

import org.eclipse.egit.github.core.event.{IssuesPayload => GhIssuePayload, PullRequestPayload => GhPullRequestPayload, PushPayload => GhPushPayload}
import org.eclipse.egit.github.core.{Commit => GhCommit, CommitUser => GhCommitUser, PullRequest => GhPullRequest}

import scala.collection.JavaConversions._

object ImplicitPayloadHandlers {

  trait EventPayloadHandler {
    def transformPayload: EventPayload
  }

  implicit class PullRequestPayloadHandler(pullRequestPayload: GhPullRequestPayload)
    extends EventPayloadHandler {

    override def transformPayload: EventPayload = {
      val action = pullRequestPayload.getAction
      val number = pullRequestPayload.getNumber
      val pullRequest = pullRequestPayload.getPullRequest
      val url = pullRequest.getHtmlUrl
      val title = pullRequest.getTitle
      val stats = extractStats(pullRequest)
      PullRequestPayload(action, number, url, title, stats)
    }

    private def extractStats(pullRequest: GhPullRequest): Stats = {
      val additions = pullRequest.getAdditions
      val deletions = pullRequest.getDeletions
      val commits = pullRequest.getCommits
      Stats(commits, additions, deletions)
    }
  }

  implicit class IssuePayloadHandler(issuePayload: GhIssuePayload)
    extends EventPayloadHandler {

    override def transformPayload: EventPayload = {
      val action = issuePayload.getAction
      val issue = issuePayload.getIssue
      val number = issue.getNumber
      val title = issue.getTitle
      val url = issue.getHtmlUrl
      IssuePayload(action, number, title, url)
    }
  }

  implicit class PushPayloadHandler(pushPayload: GhPushPayload)
    extends EventPayloadHandler {

    override def transformPayload: EventPayload = {
      val ref = pushPayload.getRef
      val before = pushPayload.getBefore
      val head = pushPayload.getHead
      val commits = pushPayload.getCommits.map(transformCommit).toList
      PushPayload(ref, before, head, commits)
    }

    private def transformCommit(commit: GhCommit) =
      Commit(commit.getSha, commit.getMessage, transformAuthor(commit.getAuthor))

    private def transformAuthor(author: GhCommitUser) =
      Author(author.getName, author.getEmail)
  }
}