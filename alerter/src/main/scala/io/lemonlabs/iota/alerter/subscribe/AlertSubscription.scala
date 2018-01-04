package io.lemonlabs.iota.alerter.subscribe

import spray.json.RootJsonFormat

case class AlertSubscription(email: String, iotaAddress: String)

object AlertSubscription {
  import spray.json.DefaultJsonProtocol._
  implicit val fmt: RootJsonFormat[AlertSubscription] = jsonFormat2(AlertSubscription.apply)
}
