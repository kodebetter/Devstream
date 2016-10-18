package com.devstream.converter

import com.devstream.apiclients.stackexchange.response.{UserTimeLineComment, UserTimeLineQuestionAnswered, UserTimeLineQuestionAsked, UserTimelineBase}
import com.devstream.model.{DevStreamEvent, DevStreamUser, PostComment, ProviderBasedEvent, QuestionAnswered, QuestionAsked}

object StackOverFlowEventConverters {

  sealed implicit class RichUserTimeLineBase(userTimelineBase: UserTimelineBase) extends EventConverters {

    override def asDevStreamEvent(user: DevStreamUser): Option[DevStreamEvent] = {
      userTimelineBase match {
        case event: UserTimeLineQuestionAsked =>
          Some(DevStreamEvent("stackoverflow", event.timelineType.toString,
            event.asProviderBasedEvent, event.timeStamp, asUserPayload(user)))

        case event: UserTimeLineQuestionAnswered =>
          Some(DevStreamEvent("stackoverflow", event.timelineType.toString,
            event.asProviderBasedEvent, event.timeStamp, asUserPayload(user)))

        case event: UserTimeLineComment =>
          Some(DevStreamEvent("stackoverflow", event.timelineType.toString,
            event.asProviderBasedEvent, event.timeStamp, asUserPayload(user)))

        case _ => None
      }
    }
  }

  private sealed trait ProviderBasedEventConverter {
    def asProviderBasedEvent: ProviderBasedEvent
  }

  private sealed implicit class QuestionAskedEventConverter(event: UserTimeLineQuestionAsked)
    extends ProviderBasedEventConverter {
    override def asProviderBasedEvent: ProviderBasedEvent =
      QuestionAsked(event.eventId, event.userId, event.link, event.questionId, event.title)
  }

  private sealed implicit class QuestionAnsweredEventConverter(event: UserTimeLineQuestionAnswered)
    extends ProviderBasedEventConverter {
    override def asProviderBasedEvent: ProviderBasedEvent =
      QuestionAnswered(event.eventId, event.userId, event.link, event.questionId, event.title)
  }

  private sealed implicit class TimeLineCommentConverter(event: UserTimeLineComment)
    extends ProviderBasedEventConverter {
    override def asProviderBasedEvent: ProviderBasedEvent =
      PostComment(event.eventId, event.userId, event.link, event.postId, event.postType.toString,
        event.questionId, event.title, event.detail)
  }

}
