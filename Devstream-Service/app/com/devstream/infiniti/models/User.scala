package com.devstream.infiniti.models

/**
  * Created by sandeept on 21/9/16.
  */


case class UserDetails(userName: String,devStreamUserId: String, providers: Seq[Provider])

case class Provider(providerName: String, providerUserId: String, authtoken: String)


object User {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._


  //TODO add the code to update user details in ES.

  def save(jsValue: JsValue) = {

    implicit val providers: Reads[Provider] = (
      (JsPath \ "providerName").read[String] and
        (JsPath \ "userId").read[String] and
        (JsPath \ "authToken").read[String]
      ) (Provider.apply _)

    implicit val userDetails: Reads[UserDetails] = (
      (JsPath \ "name").read[String] and
        (JsPath \ "devStreamUserId").read[String] and
        (JsPath \ "providers").read[Seq[Provider]]
      ) (UserDetails.apply _)

    val userData = jsValue.validate[UserDetails]
    userData.map {
      detail => println(detail.userName)
        detail.providers.foreach {
          providerDetail => println(providerDetail.authtoken)
            println(providerDetail.providerUserId)
            println(providerDetail.providerName)
        }
    }
  }

  def update(jsValue: JsValue) = {

    implicit val providers: Reads[Provider] = (
      (JsPath \ "providerName").read[String] and
        (JsPath \ "userId").read[String] and
        (JsPath \ "authToken").read[String]
      ) (Provider.apply _)

    implicit val userDetails: Reads[UserDetails] = (
      (JsPath \ "name").read[String] and
        (JsPath \ "devStreamUserId").read[String] and
        (JsPath \ "providers").read[Seq[Provider]]
      ) (UserDetails.apply _)

    val userData = jsValue.validate[UserDetails]
    userData.map {
      detail => println(detail.userName)
        detail.providers.foreach {
          providerDetail => println(providerDetail.authtoken)
            println(providerDetail.providerUserId)
            println(providerDetail.providerName)
        }
    }
  }



}
