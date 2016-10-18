package com.devstream.converter

import com.devstream.model.{DevStreamEvent, DevStreamUser, UserPayload}

trait EventConverters {

  protected def asUserPayload(user: DevStreamUser) = {
    val ldapId = user.ldapId
    val designation = user.designation
    val emailId = user.emailId
    val firstName = user.firstName
    val lastName = user.lastName
    val workLocation = user.workLocation
    val employeeId = user.employeeId
    val imageURL = getImageURL(user)
    UserPayload(ldapId, emailId, employeeId, firstName,
      lastName, workLocation, designation, imageURL)
  }

  def getImageURL(user: DevStreamUser): String = user.profiles
    .find(_.providerName.toLowerCase == "github")
    .flatMap(_.payload.get("avatar_url")
      .asInstanceOf[Option[String]]).getOrElse("")

  def asDevStreamEvent(user: DevStreamUser): Option[DevStreamEvent]
}
