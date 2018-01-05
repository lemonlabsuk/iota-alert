package io.lemonlabs.iota.alerter.feed

import java.util.concurrent.TimeUnit

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws._
import akka.stream.Materializer
import akka.stream.scaladsl._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}

case class TangleUpdate(hash: String,
                        address: String,
                        value: Long,
                        tag: String,
                        timestamp: String,
                        currentIndex: String,
                        lastIndex: String,
                        bundleHash: String,
                        trunkTransaction: String,
                        branchTransaction: String,
                        arrivalTime: String,
                        emailSubject: Option[String] = None)
object TangleUpdate {

  import DefaultJsonProtocol._

  implicit val fmt: RootJsonFormat[TangleUpdate] = jsonFormat12(TangleUpdate.apply)
}

class BloxTangleWebSocket {

  def tangleUpdatesFeed()(implicit system: ActorSystem, materializer: Materializer): Source[TangleUpdate, NotUsed] = {

    import system.dispatcher

    def logUpgradeResponse(upgradeF: Future[WebSocketUpgradeResponse]): NotUsed = {
      upgradeF.foreach { upgrade =>
        if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
          println ("Connected to wss://tangle.blox.pm:8081")
        } else {
          println (s"Connection failed to wss://tangle.blox.pm:8081: ${upgrade.response.status}")
        }
      }
      NotUsed
    }

    val flow = Http().webSocketClientFlow(WebSocketRequest("wss://tangle.blox.pm:8081/")).map {
      case TextMessage.Strict(text) =>
        import spray.json._
        val update = text.parseJson.convertTo[TangleUpdate]
        update
      case other =>
        throw new IllegalArgumentException(s"Invalid message: $other")
    }

    Source.maybe[Message].viaMat(flow)(Keep.right).mapMaterializedValue(logUpgradeResponse)
  }
}
