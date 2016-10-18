package com.devstream.model

case class QuestionAsked(eventId: String, userId: String, link: String, questionId: String, title: String)
  extends ProviderBasedEvent

case class QuestionAnswered(eventId: String, userId: String, link: String, questionId: String, title: String)
  extends ProviderBasedEvent

case class PostComment(eventId: String, userId: String, link: String, questionId: String, postId : String, postType: String,
                       title: String, details: String)
  extends ProviderBasedEvent