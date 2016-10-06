package com.devstream.provider.github

import com.devstream.provider.base.ProviderBasedEvent

case class Repo(id: Long, name: String, url: String)

case class User(userId: Long, login: String, url: String, imageUrl: String)

case class Event(id: String, user: User, repo: Repo,
                 payload: EventPayload) extends ProviderBasedEvent

case class Stats(commits: Int, additions: Long, deletions: Long)

case class Author(name: String, email: String)

case class Commit(sha: String, message: String, author: Author)

/* Various ghEvent payloads, will add more */

trait EventPayload

case class PullRequestPayload(action: String, number: Long, url: String,
                              title: String, stats: Stats) extends EventPayload

case class PushPayload(ref: String, before: String,
                       head: String, commits: List[Commit]) extends EventPayload

case class IssuePayload(action: String, number: Long,
                        title: String, url: String) extends EventPayload