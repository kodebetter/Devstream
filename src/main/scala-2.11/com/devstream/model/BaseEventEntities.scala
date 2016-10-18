package com.devstream.model

trait ProviderBasedEvent

case class Profile(providerName: String, authToken: String,
                   lastSeen: String, payload: Map[String, AnyRef])

case class DevStreamUser(ldapId: String, emailId: String, employeeId: String, firstName: String,
                         lastName: String, workLocation: String, designation: String,
                         profiles: List[Profile])

case class UserPayload(ldapId: String, emailId: String, employeeId: String, firstName: String,
                       lastName: String, workLocation: String,
                       designation: String, imageURL: String)

case class DevStreamEvent(provider: String, `type`: String, event: ProviderBasedEvent,
                          createdAt: Long, user: UserPayload)