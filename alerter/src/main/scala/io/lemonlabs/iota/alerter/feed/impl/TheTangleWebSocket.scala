package io.lemonlabs.iota.alerter.feed.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.{Origin, `User-Agent`}
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Source}
import io.lemonlabs.iota.alerter.feed.{LogWebSocketUpgrade, Transaction}
import spray.json.RootJsonFormat

case class TheTangleTransaction(transaction: Transaction)
object TheTangleTransaction {
  import spray.json.DefaultJsonProtocol._
  implicit val fmt: RootJsonFormat[TheTangleTransaction] = jsonFormat1(TheTangleTransaction.apply)
}

class TheTangleWebSocket()(implicit val system: ActorSystem, materializer: Materializer) extends LogWebSocketUpgrade {

  val log = Logging(system, this.getClass)

  val url = "wss://api.thetangle.org/v1/live"
  val sendMessage = TextMessage("""{"subscriptions":{"transactions":true}}""")

  val tangleUpdatesFeed: Source[Transaction, NotUsed] = {
    val request = WebSocketRequest(url, List(Origin("https://thetangle.org")))

    val flow = Http().webSocketClientFlow(request).map {
      case TextMessage.Strict(text) =>
        import spray.json._
        val transaction = text.parseJson.convertTo[TheTangleTransaction].transaction

        if(transaction.address.length != 81) {
          log.warning("Received address that is NOT 81 characters: {}", transaction.address)
        }

        transaction
      case other =>
        throw new IllegalArgumentException(s"Invalid message: $other")
    }

    Source.single(sendMessage)
      .concat(Source.maybe[Message])
      .viaMat(flow)(Keep.right)
      .mapMaterializedValue(logUpgradeResponse)
  }
}
