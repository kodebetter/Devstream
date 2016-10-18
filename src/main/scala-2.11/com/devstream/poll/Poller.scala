package com.devstream.poll

import com.devstream.model.{DevStreamEvent, DevStreamUser, Profile}
import com.devstream.utils.EsUtils

trait Poller {

  def pollForEvents(user: DevStreamUser, profile: Profile): List[DevStreamEvent]

  protected def updateLastSeen(userId: String, providerName: String, lastSeen: String) =
    EsUtils.updateLastSeen(userId, providerName, lastSeen)
}