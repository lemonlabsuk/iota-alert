package io.lemonlabs.iota.alerter.feed

import spray.json.RootJsonFormat

case class Transaction(hash: String,
                       address: String,
                       value: Long,
                       tag: String,
                       bundle: String,
                       trunkTransaction: String,
                       branchTransaction: String,
                       receivedAt: Long,
                       emailSubject: Option[String] = None)

object Transaction {
  import spray.json.DefaultJsonProtocol._
  implicit val fmt: RootJsonFormat[Transaction] = jsonFormat9(Transaction.apply)
}