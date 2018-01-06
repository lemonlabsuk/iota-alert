package io.lemonlabs.iota.alerter.feed.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws._
import akka.stream.Materializer
import akka.stream.scaladsl._
import io.lemonlabs.iota.alerter.feed.{LogWebSocketUpgrade, Transaction}

class BloxTangleWebSocket()(implicit val system: ActorSystem, materializer: Materializer) extends LogWebSocketUpgrade  {

  val url = "wss://tangle.blox.pm:8081/"

  val tangleUpdatesFeed: Source[Transaction, NotUsed] = {
    val flow = Http().webSocketClientFlow(WebSocketRequest(url)).map {
      case TextMessage.Strict(text) =>
        import spray.json._
          //TODO: bundleHash -> bundle
          //TODO: timestamp.toLong -> receivedAt
        val update = text.parseJson.convertTo[Transaction]
        update
      case other =>
        throw new IllegalArgumentException(s"Invalid message: $other")
    }

    Source.maybe[Message].viaMat(flow)(Keep.right).mapMaterializedValue(logUpgradeResponse)
  }
}
