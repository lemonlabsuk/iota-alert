package io.lemonlabs.iota.alerter

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws._
import akka.stream.Materializer
import akka.stream.scaladsl._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.Future

case class TransactionUpdate(hash: String,
                              address: String,
                              value: Long,
                              tag: String,
                              timestamp: String,
                              currentIndex: String,
                              lastIndex: String,
                              bundleHash: String,
                              trunkTransaction: String,
                              branchTransaction: String,
                              arrivalTime: String)
object TransactionUpdate {
  import DefaultJsonProtocol._
  implicit val jsonFormat: RootJsonFormat[TransactionUpdate] = jsonFormat11(TransactionUpdate.apply)
}

class BloxTangleWebSocket {

  def transactionUpdates()(implicit system: ActorSystem, materializer: Materializer): Source[TransactionUpdate, Future[WebSocketUpgradeResponse]] = {
    val flow = Http().webSocketClientFlow(WebSocketRequest("wss://tangle.blox.pm:8081/")).map {
      case TextMessage.Strict(text) =>
        import spray.json._
        text.parseJson.convertTo[TransactionUpdate]
      case other =>
        throw new IllegalArgumentException(s"Invalid message: $other")
    }

    Source.maybe[Message].viaMat(flow)(Keep.right)
  }
}
