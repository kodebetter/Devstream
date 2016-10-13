package com.devstream.infiniti.models

import com.devstream.infiniti.utils.Helper

import scala.xml.Node

/**
  * Created by sandeept on 21/9/16.
  */


case class UserDetails(employeeId: String, profiles: Seq[Profile])

case class EmployeeDetails(ldapId: String, emailId: String, employeeId: String, firstName: String, lastName: String,
                           workLocation: String, designation: String, profiles: List[Profile])

case class Profile(providerName: String, authtoken: String, lastSeen: String, var payLoad: Map[String, AnyRef])


object User {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  def update(jsValue: JsValue): UserDetails = {
    implicit val providers: Reads[Profile] = (
      (JsPath \ "providerName").read[String] and
        (JsPath \ "authToken").read[String] and
        (JsPath \ "lastSeen").read[String] and
        (JsPath \ "payload").read[Map[String,String]]
      ) (Profile.apply _)

    implicit val userDetails: Reads[UserDetails] = (
      (JsPath \ "employeeId").read[String] and
        (JsPath \ "profiles").read[Seq[Profile]]
      ) (UserDetails.apply _)

    val userData = jsValue.validate[UserDetails]
    userData.get
  }


  def extractDetails(data: Node): EmployeeDetails = {
    import scala.collection.JavaConverters._
    val metadata: java.util.Map[java.lang.String, java.lang.String] = Helper.transformXML(data, "attributes").asJava
    EmployeeDetails(metadata.get("uid"), metadata.get("mail"): String, metadata.get("employeenumber"): String,
      metadata.get("firstname"): String,metadata.get("lastname"): String,metadata.get("worklocation"): String,
      metadata.get("jobtitle"): String, List.empty[Profile])
  }

  def extractDetailsFromJson(data: JsValue) = {
     Helper.transformJson(data)
  }

}
