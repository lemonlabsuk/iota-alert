package io.lemonlabs.iota.alerter.feed

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.WebSocketUpgradeResponse

import scala.concurrent.Future

trait LogWebSocketUpgrade {

  def url: String
  implicit val system: ActorSystem

  import system.dispatcher

  def logUpgradeResponse(upgradeF: Future[WebSocketUpgradeResponse]): NotUsed = {
    upgradeF.foreach { upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
        println (s"Connected to $url")
      } else {
        println (s"Connection failed to $url: ${upgrade.response.status}")
      }
    }
    NotUsed
  }
}
