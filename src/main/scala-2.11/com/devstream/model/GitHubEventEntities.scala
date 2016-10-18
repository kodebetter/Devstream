package com.devstream.model

case class Repo(id: Long, name: String, url: String)

case class User(id: Long, login: String, imageUrl: String)

case class Event(id: String, user: User, repo: Repo,
                 payload: EventPayload) extends ProviderBasedEvent

case class Stats(commits: Int, additions: Long, deletions: Long)

case class Author(name: String, email: String)

case class Commit(sha: String, message: String, author: Author, url: String)

/* Various event payloads, will add more */

trait EventPayload

case class PullRequestPayload(action: String, number: Long, url: String,
                              title: String, isMerged: Boolean, stats: Stats) extends EventPayload

case class PushPayload(ref: String, before: String,
                       head: String, commits: List[Commit]) extends EventPayload

case class IssuePayload(action: String, number: Long,
                        title: String, url: String) extends EventPayload